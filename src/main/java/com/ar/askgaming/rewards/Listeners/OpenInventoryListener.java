package com.ar.askgaming.rewards.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import com.ar.askgaming.rewards.RewardsPlugin;

public class OpenInventoryListener implements Listener{

    private final RewardsPlugin plugin;
    public OpenInventoryListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e){

        if (e.getInventory().equals(plugin.getCrateManager().getGui())) {
            plugin.getCrateManager().updateGui();
        }
    }
}
