package com.ar.askgaming.rewards.Listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.Crate;
import com.ar.askgaming.rewards.RewardsPlugin;

public class PlayerInteractListener implements Listener{

    private RewardsPlugin plugin;
    public PlayerInteractListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        if (b == null) return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Crate crate = plugin.getCrateManager().getByBlock(b);
        if (crate != null){
            boolean hasKey = false;
            ItemStack key = plugin.getCrateManager().getKeyItem(crate);
            if (key == null){
                p.sendMessage("Cant open this crate, key is not set.");
                return;
            }
            if (p.getInventory().containsAtLeast(key, 1)){
                hasKey = true;

            }
            if (!hasKey){
                p.sendMessage("You need a key to open this crate.");
                return;
            }
            for (ItemStack item : p.getInventory().getContents()){
                if (item == null) continue;
                if (item.isSimilar(key)){
                    if (item.getAmount() > 1){
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        p.getInventory().remove(item);
                    }
                    plugin.getCrateManager().handleOpenByBlock(p, crate);
                    break;
                    
                }
            }
        }
    }
}
