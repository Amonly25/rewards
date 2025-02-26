package com.ar.askgaming.rewards.Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public class PlayerData implements ConfigurationSerializable{

    private UUID uuid;
    private long lastClaim;
    private int streak_connection;
    private int playtime;
    private int votes;
    private String last_connection;
    private String referralCode;
    private List<String> referredPlayers;
    private String referredBy;
    private boolean hasClaimedReferral;
    private boolean givedRewardToReferrer;

    private File file;
    private FileConfiguration config;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.lastClaim = 0;
        this.streak_connection = 0;
        this.playtime = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        this.votes = 0;
        this.last_connection = "";
        this.referralCode = "";
        this.referredPlayers = new ArrayList<>();
        this.referredBy = "";
        this.hasClaimedReferral = false;
        this.givedRewardToReferrer = false;

    }
 
    public PlayerData(Map<String, Object> map) {
        if (map.get("last_claim") instanceof Integer){
            this.lastClaim = (int) map.get("last_claim");
        } else if (map.get("last_claim") instanceof Long){
            this.lastClaim = (long) map.get("last_claim");
        }

        this.streak_connection = (int) map.get("streak_connection");
        this.playtime = (int) map.get("playtime");
        this.votes = (int) map.get("votes");
        this.last_connection = (String) map.get("last_connection");

        Object referralCode = map.get("referral_code");
        if (referralCode != null){
            this.referralCode = (String) referralCode;
        } else {
            this.referralCode = "";
        }
        Object referredPlayers = map.get("referred_players");
        if (referredPlayers != null){
            this.referredPlayers = (List<String>) referredPlayers;
        } else {
            this.referredPlayers = new ArrayList<>();
        }

        Object referredBy = map.get("referred_by");
        if (referredBy != null){
            this.referredBy = (String) referredBy;
        } else {
            this.referredBy = "";
        }
        Object hasClaimedReferral = map.get("has_claimed_referral");
        if (hasClaimedReferral != null){
            this.hasClaimedReferral = (boolean) hasClaimedReferral;
        } else {
            this.hasClaimedReferral = false;
        }
        Object givedRewardToReferrer = map.get("gived_reward_to_referrer");
        if (givedRewardToReferrer != null){
            this.givedRewardToReferrer = (boolean) givedRewardToReferrer;
        } else {
            this.givedRewardToReferrer = false;
        }


    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("last_claim", lastClaim);
        map.put("streak_connection", streak_connection);
        map.put("playtime", playtime);
        map.put("votes", votes);   
        map.put("last_connection", last_connection); 
        map.put("referral_code", referralCode);
        map.put("referred_players", referredPlayers);
        map.put("referred_by", referredBy);
        map.put("has_claimed_referral", hasClaimedReferral);
        map.put("gived_reward_to_referrer", givedRewardToReferrer);

        return map;
    }

    //#region Getters and Setters
    public String getLast_connection() {
        return last_connection;
    }
    public void setLast_connection(String last_connection) {
        this.last_connection = last_connection;
    }

    public long getLastClaim() {
        return lastClaim;
    }
    public void setLastClaim(long lastClaim) {
        this.lastClaim = lastClaim;
    }
    public int getStreak_connection() {
        return streak_connection;
    }
    public void setStreak_connection(int streak_connection) {
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
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void setConfig(FileConfiguration config) {
        this.config = config;
    }
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}
