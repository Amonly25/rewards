package com.ar.askgaming.rewards.Listeners;

import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.ar.askgaming.rewards.RewardsPlugin;

public class EntityDismountListener implements Listener {

    private final RewardsPlugin plugin;
    public EntityDismountListener(RewardsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Item){
            ItemStack item = ((Item)event.getEntity()).getItemStack();
            if (plugin.getCrateManager().isCreateKeyItem(item)){
                Entity entity = event.getDismounted();
                if (entity instanceof Enemy){
                    
                    if (entity.isDead() && ((LivingEntity) entity).getKiller() instanceof Player){
                       // Bukkit.broadcastMessage("Player killed mob and dismounted");
                        return;
                    }
                    event.getEntity().remove();
                    //Bukkit.broadcastMessage("§cCrate key removed");
                }
            }
        }
    } 
}
