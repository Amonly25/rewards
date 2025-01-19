package com.ar.askgaming.rewards.Rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.PlayerData;

public class RewardsGui {

    private RewardsPlugin plugin;
    public RewardsGui(RewardsPlugin plugin){
        this.plugin = plugin;

        size = plugin.getConfig().getInt("gui.size",27);
        title = plugin.getConfig().getString("gui.title","Rewards").replace("&", "§");

    }

    private int size;
    private String title;

    private HashMap<Player,Inventory> playerInvs = new HashMap<>();
    
    public HashMap<Player, Inventory> getPlayerInvs() {
        return playerInvs;
    }


    private void setItem(Inventory inv, Player p){
        boolean hasDaily = plugin.getConfig().getBoolean("rewards.daily.enabled",true);

        if (hasDaily){

            ItemStack daily = new ItemStack(Material.valueOf(plugin.getConfig().getString("rewards.daily.material","CHEST_MINECART")));
            ItemMeta meta = daily.getItemMeta();
            meta.setDisplayName(plugin.getConfig().getString("rewards.daily.name","Daily Reward").replace("&", "§"));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.getDailyReward().getText(p));
            meta.setLore(lore);
            daily.setItemMeta(meta);
            inv.setItem(plugin.getConfig().getInt("rewards.daily.slot",0), daily);
        }

        boolean hasStreak = plugin.getConfig().getBoolean("rewards.streak.enabled",true);

        if (hasStreak){
            ItemStack streak = new ItemStack(Material.valueOf(plugin.getConfig().getString("rewards.streak.material","GOLD_INGOT")));
            ItemMeta meta = streak.getItemMeta();
            List<String> lore = new ArrayList<>();
            PlayerData pData = plugin.getDataManager().getPlayerData(p);
            lore.add(plugin.getLangManager().getFrom("rewards.current_streak", p).replace("{streak}", pData.getStreak_connection()+""));
            meta.setLore(lore);
            meta.setDisplayName(plugin.getConfig().getString("rewards.streak.name","Streak Reward").replace("&", "§"));
            streak.setItemMeta(meta);
            inv.setItem(plugin.getConfig().getInt("rewards.streak.slot",3), streak);
        }
    }

    private Inventory createGui(Player p){
        Inventory inv = Bukkit.createInventory(null, size, title);
        playerInvs.put(p, inv);
        return inv;
    }

    public void openGui(Player p){
        Inventory inv;
        if (playerInvs.containsKey(p)){
            inv = playerInvs.get(p);
            
            p.openInventory(playerInvs.get(p));
            setItem(inv, p);
            return;
        }
        inv = createGui(p);
        if (inv == null) return;
        setItem(inv, p);
        p.openInventory(inv);

    }
}
