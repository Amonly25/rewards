package com.ar.askgaming.rewards.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.ar.askgaming.rewards.RewardsPlugin;

public class RewardsPlayerData {

    private long lastDailyClaim;
    private long lastWeeklyClaim;
    private long lastMonthlyClaim;  

    private int streak_connection;
    private int playtime;
    private int votes;
    private String last_connection;
    private String referralCode;
    private List<String> referredPlayers;
    private String referredBy;
    private boolean hasClaimedReferral;
    private boolean givedRewardToReferrer;
    private UUID playerUUID;

    private RewardsPlugin plugin = RewardsPlugin.getPlugin(RewardsPlugin.class);

    public RewardsPlayerData(UUID uuid, int streak_connection, int playtime, int votes, 
        String last_connection, String referralCode, List<String> referredPlayers, String referredBy,
        boolean hasClaimedReferral, boolean givedRewardToReferrer, long lastDailyClaim, long lastWeeklyClaim, long lastMonthlyClaim) {

        this.playerUUID = uuid;
        this.lastDailyClaim = lastDailyClaim;
        this.lastWeeklyClaim = lastWeeklyClaim;
        this.lastMonthlyClaim = lastMonthlyClaim;
        this.streak_connection = streak_connection;
        this.playtime = playtime;
        this.votes = votes;
        this.last_connection = last_connection;
        this.referralCode = referralCode;
        this.referredPlayers = referredPlayers;
        this.referredBy = referredBy;
        this.hasClaimedReferral = hasClaimedReferral;
        this.givedRewardToReferrer = givedRewardToReferrer;

    }

    public String getLastConnection() {
        return last_connection;
    }
    public void setLastConnection(String last_connection) {
        this.last_connection = last_connection;
    }

    public int getStreakConnection() {
        return streak_connection;
    }
    public void setStreakConnection(int streak_connection) {
        this.streak_connection = streak_connection;
    }
    public int getPlaytime() {
        return playtime;
    }
    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }
    public int getVotes() {
        return votes;
    }
    public void setVotes(int votes) {
        this.votes = votes;
    }
    public String getReferralCode() {
        return referralCode;
    }
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;

    }
    public List<String> getReferredPlayers() {
        return referredPlayers;
    }
    public void setReferredPlayers(List<String> referredPlayers) {
        this.referredPlayers = referredPlayers;

    }
    public String getReferredBy() {
        return referredBy;
    }
    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;

    }
    public boolean isHasClaimedReferral() {
        return hasClaimedReferral;
    }
    public void setHasClaimedReferral(boolean hasClaimedReferral) {
        this.hasClaimedReferral = hasClaimedReferral;

    }
    public boolean isGivedRewardToReferrer() {
        return givedRewardToReferrer;
    }
    public void setGivedRewardToReferrer(boolean givedRewardToReferrer) {
        this.givedRewardToReferrer = givedRewardToReferrer;

    }
    public boolean save() {
        String sql = "UPDATE rewards_data SET lastDailyClaim = ?, lastWeeklyClaim = ?, lastMonthlyClaim = ?, " +
                     "streakConnection = ?, playtime = ?, votes = ?, lastConnection = ?, referralCode = ?, " +
                     "referredPlayers = ?, referredBy = ?, hasClaimedReferral = ?, givedRewardToReferrer = ? " +
                     "WHERE uuid = ?";
    
        try (Connection con = plugin.getDatabaseManager().connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {
    
            stmt.setLong(1, lastDailyClaim);
            stmt.setLong(2, lastWeeklyClaim);
            stmt.setLong(3, lastMonthlyClaim);
            stmt.setInt(4, streak_connection);
            stmt.setInt(5, playtime);
            stmt.setInt(6, votes);
            stmt.setString(7, last_connection); // Evitar valores NULL
            stmt.setString(8, referralCode);
            
            // Guardar lista de referidos correctamente
            stmt.setString(9, String.join(",", referredPlayers)); 
            
            stmt.setString(10, referredBy != null ? referredBy : "");
            stmt.setBoolean(11, hasClaimedReferral);
            stmt.setBoolean(12, givedRewardToReferrer);
            stmt.setString(13, playerUUID.toString());
    
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public long getLastDailyClaim() {
        return lastDailyClaim;
    }

    public void setLastDailyClaim(long lastDailyClaim) {
        this.lastDailyClaim = lastDailyClaim;
    }
    public long getLastWeeklyClaim() {
        return lastWeeklyClaim;
    }

    public void setLastWeeklyClaim(long lastWeeklyClaim) {
        this.lastWeeklyClaim = lastWeeklyClaim;
    }

    public long getLastMonthlyClaim() {
        return lastMonthlyClaim;
    }

    public void setLastMonthlyClaim(long lastMonthlyClaim) {
        this.lastMonthlyClaim = lastMonthlyClaim;
    }
}
