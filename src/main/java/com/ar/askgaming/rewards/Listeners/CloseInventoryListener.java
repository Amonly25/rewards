package com.ar.askgaming.rewards.Listeners;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.Crate;
import com.ar.askgaming.rewards.RewardsPlugin;

public class CloseInventoryListener implements Listener{

    private RewardsPlugin plugin;
    public CloseInventoryListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void closeInventory(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();
        Iterator<Map.Entry<Crate, Inventory>> iterator = plugin.getCrateManager().getEditing().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Crate, Inventory> entry = iterator.next();
            if (entry.getValue().equals(inv)) {
                Crate crate = entry.getKey();
                ItemStack[] rewards = inv.getContents();
                crate.setRewards(rewards);
                plugin.getCrateManager().save();
                iterator.remove();
                p.sendMessage("§aCrate rewards saved!");
            }
        }
    }

}
