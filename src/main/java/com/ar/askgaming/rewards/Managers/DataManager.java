package com.ar.askgaming.rewards.Managers;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ar.askgaming.rewards.RewardsPlugin;

public class DataManager {

    private RewardsPlugin plugin;

    public DataManager(RewardsPlugin plugin){
        this.plugin = plugin;
    }

    public void convertData(UUID uuid) {

        File file = new File(plugin.getDataFolder() + "/playerdata", uuid.toString() + ".yml");

        if (!file.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Object obj = config.get(uuid.toString());

        if (obj instanceof PlayerData) {
            
            PlayerData pd = (PlayerData) obj;
            RewardsPlayerData rpd = plugin.getDatabaseManager().loadPlayerData(uuid);
            rpd.setLastConnection(pd.getLast_connection());
            rpd.setLastDailyClaim(pd.getLastClaim());
            rpd.setPlaytime(pd.getPlaytime());
            rpd.setStreakConnection(pd.getStreak_connection());
            rpd.setVotes(pd.getVotes());
            rpd.setReferralCode(pd.getReferralCode());
            rpd.setReferredPlayers(pd.getReferredPlayers());
            rpd.setReferredBy(pd.getReferredBy());
            rpd.setHasClaimedReferral(pd.isHasClaimedReferral());
            rpd.setGivedRewardToReferrer(pd.isGivedRewardToReferrer());
            rpd.save();
            // then remove the old file
            file.delete();
            
        }
    }


    public HashMap<String,Integer> getPlaytimeTop(){
        HashMap<String,Integer> top = new HashMap<>();
        
        File folder = new File(plugin.getDataFolder(), "/playerdata");
        File[] files = folder.listFiles();
        for (File file : files) {
            FileConfiguration cfg = new YamlConfiguration();
            try {
                cfg.load(file);
                String uuid = file.getName().replace(".yml", "");
                PlayerData pd = (PlayerData) cfg.get(uuid);
                top.put(uuid, pd.getPlaytime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return top;
    }

    
}
