package com.ar.askgaming.rewards.Managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Statistic;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class PlayerData implements ConfigurationSerializable{

    private File file;
    private FileConfiguration config;

    private RewardsPlugin plugin = RewardsPlugin.getPlugin(RewardsPlugin.class);

    public PlayerData(Player player) {
        this.player = player;

        this.lastClaim = 0;
        this.streak_connection = 0;
        this.playtime = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        this.votes = 0;
        this.last_connection = "";

        file = new File(plugin.getDataFolder() + "/playerdata", player.getUniqueId() + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();

                config = new YamlConfiguration();

                config.load(file);
                config.set(player.getUniqueId().toString(), this);
                save();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }
    public void save(){
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("last_claim", lastClaim);
        map.put("streak_connection", streak_connection);
        map.put("playtime", playtime);
        map.put("votes", votes);   
        map.put("last_connection", last_connection); 

        return map;
    }

    private Player player;
    private long lastClaim;
    private int streak_connection;
    private int playtime;
    private int votes;
    private String last_connection;

    public String getLast_connection() {
        return last_connection;
    }
    public void setLast_connection(String last_connection) {
        this.last_connection = last_connection;
    }
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
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


}
