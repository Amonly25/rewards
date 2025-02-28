package com.ar.askgaming.rewards.Managers;

import java.io.File;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class LangManager {

    private File defaultLang;
    private HashMap<String, HashMap<String, String>> cache = new HashMap<>();
    
    private RewardsPlugin plugin;
    public LangManager(RewardsPlugin plugin){
        this.plugin = plugin;

        createFile("es");
        // En must be at the end to be the default language
        createFile("en");    
    }

    private void createFile(String locale) {
        defaultLang = new File(plugin.getDataFolder() + "/lang/" + locale + ".yml");
        if (!defaultLang.exists()) {
            plugin.saveResource("lang/" + locale + ".yml", false);
        }
    }

    public void load() {
        cache.clear();
    }

    public String getFrom(String path, Player p) {
        String locale = (p == null) ? "en" : p.getLocale().split("_")[0];
    
        // Si el idioma no existe, usar inglés como fallback inmediato
        File file = new File(plugin.getDataFolder() + "/lang/" + locale + ".yml");
        if (!file.exists()) {
            locale = "en";
            file = defaultLang;
        }
    
        // Verificar caché
        if (cache.containsKey(locale) && cache.get(locale).containsKey(path)) {
            return ChatColor.translateAlternateColorCodes('&', cache.get(locale).get(path));
        }
    
        // Cargar mensaje desde archivo
        String required = loadMessage(file, path);
    
        // Si el mensaje no se encuentra en el archivo de idioma, obtener el de inglés
        if (required.startsWith("Error:")) {
            required = loadMessage(defaultLang, path);
        }
    
        // Guardar en caché
        cache.computeIfAbsent(locale, k -> new HashMap<>()).put(path, required);
        return ChatColor.translateAlternateColorCodes('&', required);
    }
    
    private String loadMessage(File file, String path) {
        FileConfiguration langFile = YamlConfiguration.loadConfiguration(file);
    
        if (langFile.isList(path)) {
            return String.join("\n", langFile.getStringList(path));
        }
    
        return langFile.getString(path, "Error: Invalid lang path: " + path);  
    } 
}
