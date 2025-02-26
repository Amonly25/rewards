package com.ar.askgaming.rewards.Referrals;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.DatabaseManager;
import com.ar.askgaming.rewards.Managers.RewardsPlayerData;

public class ReferralsManager extends BukkitRunnable{

    private RewardsPlugin plugin;
    private DatabaseManager databaseManager;

    public ReferralsManager(RewardsPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        new Commands(plugin, this);

        runTaskTimer(plugin, 20*60, 20*60);
    }

    private final Random random = new SecureRandom();
    private HashMap<Player,Long> messages = new HashMap<>();

    private RewardsPlayerData getData(UUID uuid) {
        return databaseManager.loadPlayerData(uuid);
    }
    //#region Create code
    public void createReferralCode(Player player) {

        String code;
        do {
            String subString = player.getName().substring(0, Math.min(3, player.getName().length())).toUpperCase();
            int randomNumber = 1000 + random.nextInt(9000);
            code = subString + randomNumber;
        } while (existsCode(code)); // Sigue generando mientras el código ya exista.

        RewardsPlayerData data = getData(player.getUniqueId());
        data.setReferralCode(code);
        data.save();
        player.sendMessage("Your referral code is: " + code);
    }
    //#region Get code
    public String getRefferalCode(Player player) {
        RewardsPlayerData data = getData(player.getUniqueId());
        String code = data.getReferralCode();
        if (code == null || code.isBlank()) return "Use /referral getcode";
        return code;
    }
    public String getReferralCode(String playerName) {

        @SuppressWarnings("deprecation")
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);

        RewardsPlayerData data = getData(player.getUniqueId());
        if (data == null) return "Not found";
        String code = data.getReferralCode();
        if (code == null || code.isBlank())  return "Not Created";
        return code;
        
    }
    //#region Exists code
    public boolean existsCode(String code) {
        String query = "SELECT 1 FROM refferals_codes WHERE code = ? LIMIT 1;";
        try (PreparedStatement stmt = plugin.getDatabaseManager().connect().prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Si hay algún resultado, el código existe
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Error o código no encontrado
    }
    //#region On Join
    public void sendMessage(Player p) {
        // is first join?

        if (Bukkit.getServer().getOnlineMode() == false) {
            if (plugin.getAuthMeApi() != null) {
                if (!plugin.getAuthMeApi().isAuthenticated(p)) {
                    return;
                }
            }
        }

        int playtime = plugin.getPlaytimeManager().getPlaytimeMinutes(p);
        if (playtime >= 60) {
            return;
        }
        RewardsPlayerData data = getData(p.getUniqueId());
        if (data.getReferredBy() != null && !data.getReferredBy().isEmpty()) {
            return;
        }
        p.sendMessage("You has been referred by someone? Use /referral <code>, to claim your reward.");

    }
    //#region On Referral
    public void onReferral(Player sender, String supposedCode){

        //Process refferer
        OfflinePlayer referrer = getReferredBy(supposedCode);
        if (referrer == null) {
            sender.sendMessage("Invalid code.");
            return;
        }
        RewardsPlayerData referrerData = getData(referrer.getUniqueId());
        List<String> referredPlayers = referrerData.getReferredPlayers();
        if (!referredPlayers.contains(sender.getName())) {
            referredPlayers.add(sender.getName());
            referrerData.setReferredPlayers(referredPlayers);
            referrerData.save();
            addCodeUseToDatabase(referrer.getUniqueId());
            Player playerOnline = referrer.getPlayer();
            if (playerOnline != null) {
                playerOnline.sendMessage("You have a new referred player: " + sender.getName());
                return;
            }
        }


        //Process referred
        sender.sendMessage("Code claimed successfully.");
        RewardsPlayerData data = getData(sender.getUniqueId());
        data.setHasClaimedReferral(true);
        data.setReferredBy(referrer.getName());
        data.save();

        giveRewardToReffered(sender);
        
    }
    //#region Get referred by
    public OfflinePlayer getReferredBy(String code) {
        String query = "SELECT uuid FROM refferals_codes WHERE code = ?;";
        try (PreparedStatement stmt = plugin.getDatabaseManager().connect().prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                return Bukkit.getOfflinePlayer(uuid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // No se encontró el código o hubo un error
    }
    //#region Run
    @Override
    public void run() {
        
        int max_minutes = plugin.getConfig().getInt("referral.after_how_much_playtime", 30);

        for (Player p : plugin.getServer().getOnlinePlayers()) {

            if (messages.containsKey(p)) {
                if (System.currentTimeMillis() - messages.get(p) < 1000*60*5) {
                    continue;
                }
            } else{
                messages.put(p, System.currentTimeMillis());
                sendMessage(p);
            }

            RewardsPlayerData data = getData(p.getUniqueId());
            if (data.getReferredBy() == null || data.getReferredBy().isEmpty()) continue;
            if (data.isGivedRewardToReferrer()) continue;

            int playtime = plugin.getPlaytimeManager().getPlaytimeMinutes(p);
            if (playtime < max_minutes) continue;

            giveRewardToReferrer(data.getReferredBy());
            data.setGivedRewardToReferrer(true);
            data.save();

        }
    }
    //#region Referred Rewards
    public void processReferral(Player player, String playerName){
        plugin.getLogger().info("Processing reward to referred " + player.getName() + " by " + playerName);
        giveRewardToReffered(player);
        RewardsPlayerData data = getData(player.getUniqueId());
        data.setReferredBy(playerName);
        data.save();
    }
    public void giveRewardToReffered(Player p) {
        
       List<String> commands = plugin.getConfig().getStringList("referral.rewards.to_referred.commands");
       String message = plugin.getConfig().getString("referral.rewards.to_referred.message");

        for (String s : commands) {
            s = s.replace("%player%", p.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s);
        }
        p.sendMessage(message);
    }
    //#region Referrer Rewards
    public void giveRewardToReferrer(String name) {
    List<String> commands = plugin.getConfig().getStringList("referral.rewards.to_referrer.commands");
       String message = plugin.getConfig().getString("referral.rewards.to_referrer.message");

        for (String s : commands) {
            s = s.replace("%player%", name);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s);
        }
        Player p = plugin.getServer().getPlayer(name);
        if (p != null) {
            p.sendMessage(message);
        }
    }
    //#region add
    public void addCommand(OfflinePlayer target, int amount) {
        RewardsPlayerData pData = getData(target.getUniqueId());
        String refferedBy = pData.getReferredBy();
        if (refferedBy == null || refferedBy.isEmpty()) {
            plugin.getLogger().info("Player " + target.getName() + " does not have a referrer, command not processed.");
            return;
        }
        plugin.getLogger().info("Player has been referred by " + refferedBy + ", processing command.");
        giveBuyReward(target, amount);
    }
    public void giveBuyReward(OfflinePlayer target, int amount) {
        List<String> commands = plugin.getConfig().getStringList("referral.rewards.on_buy_commands.commands");
        String message = plugin.getConfig().getString("referral.rewards.on_buy_commands.message");

        for (String s : commands) {
            s = s.replace("%player%", target.getName()).replace("%amount%", amount+"");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s);
            plugin.getLogger().info("Command processed: " + s);
        }
        Player p = plugin.getServer().getPlayer(target.getName());
        if (p != null) {
            p.sendMessage(message);
        }
    }
    public void addCodeUseToDatabase(UUID referrer) {
        String sql = "UPDATE refferals_codes SET uses = uses + 1 WHERE uuid = ?";

        try (Connection conn = plugin.getDatabaseManager().connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referrer.toString());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("No se encontró el código de referido para: " + referrer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
