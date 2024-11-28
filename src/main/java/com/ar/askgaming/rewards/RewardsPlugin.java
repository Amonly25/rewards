package com.ar.askgaming.rewards;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.rewards.Commands.CrateCommands;
import com.ar.askgaming.rewards.Commands.RewardsCommands;
import com.ar.askgaming.rewards.Listeners.BlockBreakListener;
import com.ar.askgaming.rewards.Listeners.InventoryClickListener;
import com.ar.askgaming.rewards.Listeners.OpenCrateByInteractBlockListener;
import com.ar.askgaming.rewards.Listeners.OpenInventoryListener;
import com.ar.askgaming.rewards.Listeners.PlaceBlockListener;
import com.ar.askgaming.rewards.Listeners.PlayerOpenCrateByInteractListener;

public class RewardsPlugin extends JavaPlugin {
    
    CrateManager crateManager;

    public void onEnable() {

        saveDefaultConfig();
        
        ConfigurationSerialization.registerClass(Crate.class,"Crate");

        crateManager = new CrateManager(this);

        getServer().getPluginCommand("rewards").setExecutor(new RewardsCommands());
        getServer().getPluginCommand("crate").setExecutor(new CrateCommands(this));

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new OpenCrateByInteractBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlaceBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerOpenCrateByInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new OpenInventoryListener(this), this);
    }

    public void onDisable() {
        crateManager.getGui().getViewers().forEach(viewer -> viewer.closeInventory());

        crateManager.getCrates().forEach((name, crate) -> {
            if (crate.getTextDisplay()!=null) {
                crate.getTextDisplay().remove();

            }
            if (crate.getItemDisplay()!=null) {
                crate.getItemDisplay().remove();
            }
            
        });
        
    }
    public CrateManager getCrateManager() {
        return crateManager;
    }

}