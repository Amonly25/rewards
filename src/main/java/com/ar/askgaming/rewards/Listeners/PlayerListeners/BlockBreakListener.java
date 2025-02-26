package com.ar.askgaming.rewards.Listeners.PlayerListeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.ar.askgaming.rewards.RewardsPlugin;

public class BlockBreakListener implements Listener{

    private RewardsPlugin plugin;
    public BlockBreakListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Block b = e.getBlock();
        if (plugin.getCrateManager().getByBlock(b) != null){
            e.setCancelled(true);
        }
    }
}
