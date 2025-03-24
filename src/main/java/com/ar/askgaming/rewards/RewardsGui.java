package com.ar.askgaming.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.rewards.Managers.RewardsPlayerData;

public class RewardsGui {

    private final RewardsPlugin plugin;
    private final int size;
    private final String title;
    private final HashMap<Player, Inventory> playerInvs = new HashMap<>();

    public RewardsGui(RewardsPlugin plugin) {
        this.plugin = plugin;
        this.size = plugin.getConfig().getInt("gui.size", 27);
        this.title = colorize(plugin.getConfig().getString("gui.title", "Rewards"));
    }

    public HashMap<Player, Inventory> getPlayerInvs() {
        return playerInvs;
    }

    private void setItem(Inventory inv, Player p) {
        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(p.getUniqueId());
        configureItem(inv, p, "daily", plugin.getDailyReward().getText(p));
        configureItem(inv, p, "streak", String.valueOf(data.getStreakConnection()));
        configureItem(inv, p, "vote", String.valueOf(data.getVotes()));
        configureItem(inv, p, "playtime", plugin.getPlaytimeManager().getPlaytimeFormmated(p));
        configureItem(inv, p, "referrals", data.getReferredPlayers().size() + "");
    }

    private void configureItem(Inventory inv, Player p, String key, String placeholder) {
        if (!plugin.getConfig().getBoolean("gui." + key + ".enabled", true)) return;

        ItemStack item;
        try {
            item = new ItemStack(Material.valueOf(plugin.getConfig().getString("gui." + key + ".material")));
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid material in gui." + key + ".material");
            item = new ItemStack(Material.STONE);
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(colorize(plugin.getConfig().getString("gui." + key + ".name", key + " Reward")));
        List<String> lore = plugin.getConfig().getStringList("gui." + key + ".lore");
        List<String> newLore = new ArrayList<>();
        for (String line : lore) {
            if (key.equals("referrals")){

                line = line.replace("%" + key + "%", placeholder);
                line = line.replace("%code%", plugin.getReferrals().getRefferalCode(p));
                newLore.add(colorize(line));
            } else newLore.add(colorize(line.replace("%" + key + "%", placeholder)));
        }
        meta.setLore(newLore);
        item.setItemMeta(meta);
        inv.setItem(plugin.getConfig().getInt("gui." + key + ".slot", 0), item);
    }

    private String colorize(String text) {
        return text != null ? text.replace("&", "§") : "";
    }

    private Inventory createGui(Player p) {
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