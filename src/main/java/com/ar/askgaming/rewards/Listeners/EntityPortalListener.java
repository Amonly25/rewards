package com.ar.askgaming.rewards.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.RewardsPlugin;

public class EntityPortalListener implements Listener{

    private RewardsPlugin plugin;
    public EntityPortalListener(RewardsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();
        
        // Si es un item, cancelar la teletransportación predeterminada
        if (entity instanceof Item) {
            Item item = (Item) entity;
            ItemStack itemStack = item.getItemStack();
            if (plugin.getCrateManager().isCreateKeyItem(itemStack)){
                event.setCancelled(true);
                item.remove();
            }
        }
    }
}
