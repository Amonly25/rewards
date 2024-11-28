package com.ar.askgaming.rewards.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.ar.askgaming.rewards.Crate;
import com.ar.askgaming.rewards.RewardsPlugin;

public class PlayerOpenCrateByInteractListener implements Listener{

    private RewardsPlugin plugin;
    public PlayerOpenCrateByInteractListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){

        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) return;

        if (!plugin.getCrateManager().isCreateKeyItem(item)) return;

        e.setCancelled(true);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String type = meta.getPersistentDataContainer().get(plugin.getCrateManager().getKey(), PersistentDataType.STRING);
        if (type == null) return;
        
        //Check inventory from crate gui
        Crate crate = plugin.getCrateManager().getCrateByName(type);
        if (crate == null){
           p.sendMessage("This item is not longer a crate item.");
            return;
        }
        
        ItemStack key = plugin.getCrateManager().getKeyItem(crate);
        if (key.isSimilar(item)){
            p.sendMessage("You must click the crate to open it.");
        }
        boolean hasKey = false;
        if (crate.isKeyRequired()){
            if (p.getInventory().containsAtLeast(key, 1)){
                plugin.getCrateManager().removeCrateAndStartOpening(p, item,crate);
                return;
            }
        } else {
            plugin.getCrateManager().removeCrateAndStartOpening(p, item,crate);
            return;
        }
        if (!hasKey){
            p.sendMessage("You need a key to open this crate.");
            return;
        }
    }
}
