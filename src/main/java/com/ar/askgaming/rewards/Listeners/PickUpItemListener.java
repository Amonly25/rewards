package com.ar.askgaming.rewards.Listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.ar.askgaming.rewards.RewardsPlugin;

public class PickUpItemListener implements Listener{

    private RewardsPlugin plugin;
    public PickUpItemListener(RewardsPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onEntityDeath(EntityPickupItemEvent e) {
        Item item = e.getItem();
        if (item.getVehicle() != null) {
            if (plugin.getCrateManager().isCreateKeyItem(item.getItemStack())){
                e.setCancelled(true);
            }
        }
    }
}
