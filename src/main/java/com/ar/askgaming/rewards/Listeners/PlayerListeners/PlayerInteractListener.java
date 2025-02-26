package com.ar.askgaming.rewards.Listeners.PlayerListeners;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Crates.Crate;

public class PlayerInteractListener implements Listener{

    private RewardsPlugin plugin;
    public PlayerInteractListener(RewardsPlugin plugin){
        this.plugin = plugin;
    }

    private HashMap<Player, Long> lastClick = new HashMap<>();

    @EventHandler
    public void openCrateByInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClick.getOrDefault(p, (long) 0) < 100) { // 100 ms de espera
            return; // Ignorar si el clic fue demasiado rápido
        }
        lastClick.put(p, currentTime);

        if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK){
            openCrateByInteractToBlock(e);
        } 
        if (Action.RIGHT_CLICK_AIR == e.getAction() || Action.RIGHT_CLICK_BLOCK == e.getAction()){
            if (e.getItem() == null || e.getItem().getType() == Material.AIR) return;
            openCrateByInteractToItem(e);
        }
    }

    public void openCrateByInteractToItem(PlayerInteractEvent e){

        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (!plugin.getCrateManager().isCreateKeyItem(item)) return;

        e.setCancelled(true);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String type = meta.getPersistentDataContainer().get(plugin.getCrateManager().getKey(), PersistentDataType.STRING);
        if (type == null) return;
        
        //Check inventory from crate gui
        Crate crate = plugin.getCrateManager().getCrateByName(type);
        if (crate == null){
           p.sendMessage(plugin.getLangManager().getFrom("crates.no_longer_exists", p));
            return;
        }
        
        ItemStack similar = crate.getKeyItem();
        if (similar == null || similar.getType() == Material.AIR){
            p.sendMessage("§cCrate key is not set correctly, setting default key item.");
            similar = crate.setDefaultKey();
            plugin.getCrateManager().save(crate);
            return;
        }
        if (similar.getType()== item.getType()){
            p.sendMessage(plugin.getLangManager().getFrom("crates.use_crate_to_open", p));
            return;
        }
        ItemStack key = null;
        for (ItemStack i : p.getInventory().getContents()){
            if (i == null) continue;
            if (plugin.getCrateManager().isCreateKeyItem(crate, i) && !item.equals(i)){
                key = i;
                break;
            }
        }

        if (crate.isKeyRequired() && key == null){ 
            p.sendMessage(plugin.getLangManager().getFrom("crates.need_key", p));
            return;
        }
        if (item.equals(key)){
            key = null;
        }
        if (plugin.getCrateManager().removeCrateKeyBeforetOpening(p, item, key, crate)) {
            plugin.getCrateManager().handleOpenByInventory(p, crate);
        }
    }
    public void openCrateByInteractToBlock(PlayerInteractEvent e){
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        Crate crate = plugin.getCrateManager().getByBlock(b);
        
        if (crate != null){
            e.setCancelled(true);

            ItemStack key = null;
            for (ItemStack i : p.getInventory().getContents()){
                if (i == null) continue;
                if (plugin.getCrateManager().isCreateKeyItem(crate, i)){
                    key = i;
                    break;
                }
            }
            if (key == null){
                p.sendMessage(plugin.getLangManager().getFrom("crates.need_key", p));
                return;
            }

            if (plugin.getCrateManager().removeCrateKeyBeforetOpening(p, null, key, crate)){
                plugin.getCrateManager().handleOpenByBlock(p, crate);
                return;
            }
        }
    }

}
