package com.ar.askgaming.rewards.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.RewardsPlugin;

public class PlaceBlockListener implements Listener{

    private RewardsPlugin plugin;
    public PlaceBlockListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        ItemStack item = e.getItemInHand();
        Player p = e.getPlayer();
        if (plugin.getCrateManager().isCreateKeyItem(item)){
            e.setCancelled(true);
            p.sendMessage("You can't place this block");
            //Add admin message
        }
    }

}
