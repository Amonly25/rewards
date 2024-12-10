package com.ar.askgaming.rewards.Managers;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class LangManager {

    private RewardsPlugin plugin;
    public LangManager(RewardsPlugin plugin){
        this.plugin = plugin;

        // Save default lang file from resources if it doesn't exist
        File defaultLangFile = new File(plugin.getDataFolder() + "/lang/en.yml");
        if (!defaultLangFile.exists()) {
            plugin.saveResource("lang/en.yml", false);
            
        }
        File esLangFile = new File(plugin.getDataFolder() + "/lang/es.yml");
        if (!esLangFile.exists()) {
            plugin.saveResource("lang/es.yml", false);
        }
    
    }
    public String getFrom(String path, Player p) {

        String locale = p.getLocale().split("_")[0];

        File file = new File(plugin.getDataFolder() + "/lang/" + locale + ".yml");
        String required = "";
    
        if (!file.exists()) {
            required = getDefaultLang(path);
        } else {
            FileConfiguration langFile = new YamlConfiguration();
            try {
                langFile.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
    
            if (langFile.isList(path)) {
                StringBuilder result = new StringBuilder();
                for (String s : langFile.getStringList(path)) {
                    result.append(s).append("\n");
                }
                required = result.toString().trim();
            } else {
                required = langFile.getString(path, getDefaultLang(path));
            }
        }
        
        return ChatColor.translateAlternateColorCodes('&', required);
    }

    private String getDefaultLang(String path){

        //Add cache maybe

        File file = new File(plugin.getDataFolder() + "/lang/en.yml");
        FileConfiguration lang = new YamlConfiguration();

        try {
            lang.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if (lang.isList(path)) {
            StringBuilder result = new StringBuilder();
            for (String s : lang.getStringList(path)) {
                result.append(s).append("\n");
            }
            return result.toString().trim();
        }

        return lang.getString(path,"Undefined key: " + path);
    }

    public void broadcastTranslated(String path, String replace, String replacement) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            
            player.sendMessage(getFrom(path, player).replace(replace, replacement));

        });
    }
    
}
