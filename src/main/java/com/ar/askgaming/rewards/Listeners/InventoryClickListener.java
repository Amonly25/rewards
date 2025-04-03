package com.ar.askgaming.rewards.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Crates.Crate;

public class InventoryClickListener implements Listener{

    private final RewardsPlugin plugin;
    public InventoryClickListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onRewardsGuid(InventoryClickEvent e){
       
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        Inventory actual = e.getInventory();
        for (Inventory check : plugin.getRewardsGui().getPlayerInvs().values()){
            if (check.equals(actual)){
                e.setCancelled(true);

                ItemStack item = e.getCurrentItem();
                if (item == null || item.getType().equals(Material.AIR)) {
                    break;
                }
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;
                String name = meta.getDisplayName();
                if (name == null) return;

                String daily = plugin.getConfig().getString("gui.daily.name","Daily Reward").replace("&", "§");

                if (name.equals(daily)) {
                    if (plugin.getDailyReward().canClaimDailyReward(p)){
                        plugin.getDailyReward().giveDailyReward(p);
                    } else {
                        p.sendMessage(plugin.getLangManager().getFrom("daily.cant_claim_yet", p));

                    }
                } 
            }
        }
    }
    @EventHandler
    public void onCrateInventory(InventoryClickEvent e){
       
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
    @EventHandler
    public void onPreviewCrate(InventoryClickEvent e){

        Inventory upper = e.getInventory();
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Player p = (Player) e.getWhoClicked();

        for (Inventory check : plugin.getCrateManager().getEditing().values()){
            if (check.equals(upper)){
                if (!p.hasPermission("rewards.crate.admin")){
                    e.setCancelled(true);
                }
            }
        }
    }
}
