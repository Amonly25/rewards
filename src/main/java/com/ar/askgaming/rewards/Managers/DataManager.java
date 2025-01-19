package com.ar.askgaming.rewards.Managers;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class DataManager {

    private RewardsPlugin plugin;

    public DataManager(RewardsPlugin plugin){
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder(), "/playerdata");

        if (!folder.exists()) {
            folder.mkdirs();
            return;
        }

        Bukkit.getOnlinePlayers().forEach(p -> {
            loadOrCreatePlayerData(p);
        });
    }

    private HashMap<Player, PlayerData> playerData = new HashMap<>();

    public PlayerData getPlayerData(Player p){
        return playerData.getOrDefault(p, loadOrCreatePlayerData(p));
    }

    public PlayerData loadOrCreatePlayerData(Player p) {
        if (playerData.containsKey(p)) {
            return playerData.get(p);
        }
        File file = new File(plugin.getDataFolder() + "/playerdata", p.getUniqueId() + ".yml");

        if (!file.exists()) {
            PlayerData pd = new PlayerData(p);
            playerData.put(p, pd);
            return pd;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData pd = (PlayerData) config.get(p.getUniqueId().toString());
        pd.setFile(file);
        pd.setConfig(config);
        pd.setPlayer(p);
        playerData.put(p, pd);

        return pd;

    }
}
