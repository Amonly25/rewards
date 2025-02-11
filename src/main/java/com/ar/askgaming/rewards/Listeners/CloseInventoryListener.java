package com.ar.askgaming.rewards.Listeners;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Crates.Crate;

public class CloseInventoryListener implements Listener{

    private RewardsPlugin plugin;
    public CloseInventoryListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void closeInventory(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();
        Iterator<Map.Entry<String, Inventory>> iterator = plugin.getCrateManager().getEditing().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Inventory> entry = iterator.next();
            if (entry.getValue().equals(inv)) {
                Crate crate = plugin.getCrateManager().getCrateByName(entry.getKey());
                if (crate == null) {
                    iterator.remove();
                    return;
                }
                ItemStack[] rewards = inv.getContents();
                crate.setRewards(rewards);
                plugin.getCrateManager().save(crate);
                iterator.remove();
                p.sendMessage("§aCrate rewards saved!");
            }
        }
    }

}
