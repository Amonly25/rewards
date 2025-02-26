package com.ar.askgaming.rewards.Managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class DatabaseManager {

    private final String databaseType;
    private final String databaseUrl;
    private final String username;
    private final String password;

    private final HashMap<UUID, RewardsPlayerData> playerCache = new HashMap<>();

    private RewardsPlugin plugin;
    public DatabaseManager(RewardsPlugin main) {
        plugin = main;
        databaseType = plugin.getConfig().getString("data_mode", "SQLITE").equalsIgnoreCase("mysql") ? "MYSQL" : "SQLITE";

        switch (databaseType.toUpperCase()) {
            case "SQLITE":
                databaseUrl = plugin.getDataFolder()+"/rewards.db";
                username = null;
                password = null;
                break;
            case "MYSQL":
                databaseUrl = plugin.getConfig().getString("mysql.url");
                username = plugin.getConfig().getString("mysql.username");
                password = plugin.getConfig().getString("mysql.password");
                break;
            default:
                throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            playerCache.put(p.getUniqueId(), loadPlayerData(p.getUniqueId()));
        }
    }
    public Connection connect() throws SQLException {
        switch (databaseType) {
            case "SQLITE":
                return DriverManager.getConnection("jdbc:sqlite:" + databaseUrl);
            case "MYSQL":
                return DriverManager.getConnection("jdbc:mysql://" + databaseUrl, username, password);
            default:
                throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
    }
    public void createTable(){
        String sql = "";
        switch (databaseType) {
            case "SQLITE":
                sql = "CREATE TABLE IF NOT EXISTS rewards_data ("
                        + "uuid TEXT PRIMARY KEY,"
                        + "lastDailyClaim REAL,"
                        + "lastWeeklyClaim REAL,"
                        + "lastMonthlyClaim REAL,"
                        + "streakConnection INT,"
                        + "playtime INT,"
                        + "votes INT,"
                        + "lastConnection TEXT,"
                        + "referralCode TEXT,"
                        + "referredPlayers TEXT,"
                        + "referredBy TEXT,"
                        + "hasClaimedReferral INT,"
                        + "givedRewardToFeferrer INT"
                        + ");";
                break;
            case "MYSQL":
                sql = "CREATE TABLE IF NOT EXISTS rewards_data ("
                        + "uuid VARCHAR(36) PRIMARY KEY,"
                        + "lastDailyClaim BIGINT,"
                        + "lastWeeklyClaim BIGINT,"
                        + "lastMonthlyClaim BIGINT,"
                        + "streakConnection INT,"
                        + "playtime INT,"
                        + "votes INT,"
                        + "lastConnection TEXT,"
                        + "referralCode TEXT,"
                        + "referredPlayers TEXT,"
                        + "referredBy TEXT,"
                        + "hasClaimedReferral INT,"
                        + "givedRewardToFeferrer INT"
                        + ");";
                break;
            default:
                throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
        
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RewardsPlayerData loadPlayerData(UUID uuid) {
        if (playerCache.containsKey(uuid)) {
            return playerCache.get(uuid);
        }

        String sql = "SELECT * FROM rewards_data WHERE uuid = ?;";
        try (Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Extraer valores del ResultSet
                    Long lastDailyClaim = rs.getLong("lastDailyClaim");
                    Long lastWeeklyClaim = rs.getLong("lastWeeklyClaim");
                    Long lastMonthlyClaim = rs.getLong("lastMonthlyClaim");
                    int streakConnection = rs.getInt("streakConnection");
                    int playtime = rs.getInt("playtime");
                    int votes = rs.getInt("votes");
                    String lastConnection = rs.getString("lastConnection");
                    String referralCode = rs.getString("referralCode");
                    String referredBy = rs.getString("referredBy");
                    boolean hasClaimedReferral = rs.getBoolean("hasClaimedReferral");
                    boolean givedRewardToReferrer = rs.getBoolean("givedRewardToFeferrer");

                    // Convertir una lista de jugadores referidos desde un String (si está almacenado como JSON o CSV)
                    List<String> referredPlayers = new ArrayList<>();
                    String referredPlayersRaw = rs.getString("referredPlayers");
                    if (referredPlayersRaw != null && !referredPlayersRaw.isEmpty()) {
                        referredPlayers = Arrays.asList(referredPlayersRaw.split(",")); // Suponiendo formato CSV
                    }

                    RewardsPlayerData data = new RewardsPlayerData(uuid, streakConnection, playtime, votes, lastConnection, referralCode, referredPlayers, referredBy, hasClaimedReferral, givedRewardToReferrer, lastDailyClaim, lastWeeklyClaim, lastMonthlyClaim);
                    playerCache.put(uuid, data);
                    return data;
                } else {

                    return createNew(uuid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public RewardsPlayerData createNew(UUID uuid) {
        String sql = "";
        switch (databaseType) {
            case "SQLITE":
                sql = "INSERT INTO rewards_data (uuid, lastDailyClaim, lastWeeklyClaim, lastMonthlyClaim, streakConnection, playtime, votes, lastConnection, referralCode, referredPlayers, referredBy, hasClaimedReferral, givedRewardToFeferrer) VALUES (?, 0, 0, 0, 0, 0, 0, '', '', '', '', 0, 0);";
                
                break;
            case "MYSQL":
                sql = "INSERT INTO rewards_data (uuid, lastDailyClaim, lastWeeklyClaim, lastMonthlyClaim, streakConnection, playtime, votes, lastConnection, referralCode, referredPlayers, referredBy, hasClaimedReferral, givedRewardToFeferrer) VALUES (?, 0, 0, 0, 0, 0, 0, '', '', '', '', 0, 0);";
                break;
            default:
                break;
        }
        try (Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
            return new RewardsPlayerData(uuid, 0, 0, 0, "", "", new ArrayList<>(), "", false, false, 0L, 0L, 0L);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void createRefferalsCodeTable() {
        String sql = "";
        switch (databaseType) {
            case "SQLITE":
                sql = "CREATE TABLE IF NOT EXISTS refferals_codes ("
                        + "uuid TEXT PRIMARY KEY,"
                        + "code TEXT NOT NULL,"
                        + "uses INT"
                        + ");";
                break;
            case "MYSQL":
                sql = "CREATE TABLE IF NOT EXISTS refferals_codes ("
                        + "uuid VARCHAR(36) PRIMARY KEY,"
                        + "code VARCHAR(36) NOT NULL,"
                        + "uses INT"
                        + ");";
                break;
            default:
                throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
        
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public HashMap<UUID, RewardsPlayerData> getPlayerCache() {
        return playerCache;
    }
    public String getDatabaseType() {
        return databaseType;
    }
}
