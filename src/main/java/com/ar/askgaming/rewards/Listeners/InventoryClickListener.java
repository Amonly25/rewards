package com.ar.askgaming.rewards.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.rewards.Crate;
import com.ar.askgaming.rewards.RewardsPlugin;

public class InventoryClickListener implements Listener{

    private RewardsPlugin plugin;
    public InventoryClickListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
       
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();

        if (e.getInventory().equals(plugin.getCrateManager().getGui())) {
            e.setCancelled(true);

            if (!e.getClickedInventory().equals(plugin.getCrateManager().getGui())) {
                return;
            }
                    
            ItemStack item = e.getCurrentItem();
            
            if (item == null || item.getType().equals(Material.AIR)) {
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            String name = meta.getDisplayName();
            Crate crate = plugin.getCrateManager().getCrateByName(name);
            if (crate == null) return;
            ItemStack gived = plugin.getCrateManager().getCrateItem(crate);
            
            if (p.getInventory().firstEmpty() == -1){
                p.sendMessage("§cYour inventory is full.");
                return;
            }
            if (crate.isKeyRequired()){
                p.getInventory().addItem(plugin.getCrateManager().getKeyItem(crate));
            }
            if (p.getInventory().firstEmpty() == -1){
                p.sendMessage("§cYour inventory is full.");
                return;
            }
            p.getInventory().addItem(gived);
            return;
        }
    }
}
