package com.ar.askgaming.rewards.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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
                p.sendMessage("Your inventory is full.");
                return;

            }
            p.getInventory().addItem(gived);
            return;
        }

        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        if (!plugin.getCrateManager().isCreateKeyItem(item)) return;

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
                hasKey = true;
            }
        } else {
            hasKey = true;
        }
        if (!hasKey){
            p.sendMessage("You need a key to open this crate.");
            return;
        }
        for (ItemStack i : p.getInventory().getContents()){
            if (i == null) continue;
            if (i.isSimilar(key)){
                if (i.getAmount() > 1){
                    i.setAmount(i.getAmount() - 1);
                } else {
                    p.getInventory().remove(i);
                    if (item.getAmount() > 1){
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        e.setCurrentItem(null);

                    }
                }
                plugin.getCrateManager().handleOpenByInventory(p, crate);
                break;
            }
        }
    }
}
