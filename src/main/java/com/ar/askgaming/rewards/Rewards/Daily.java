package com.ar.askgaming.rewards.Rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.PlayerData;

public class Daily {

    private RewardsPlugin plugin;
    public Daily(RewardsPlugin plugin){
        this.plugin = plugin;
    }
        //#region Daily Rewards
    public void giveDailyReward(Player p){
        List<String> blacklist = plugin.getConfig().getStringList("daily.blacklist");
        if (blacklist.isEmpty()){
        blacklist.add("COMMAND_BLOCK");
        blacklist.add("BARRIER");
        blacklist.add("LIGHT");
        blacklist.add("AIR");
        blacklist.add("BEDROCK");
        blacklist.add("CHAIN_COMMAND_BLOCK");
        blacklist.add("REPEATING_COMMAND_BLOCK");
        blacklist.add("COMMAND_BLOCK_MINECART");
        blacklist.add("STRUCTURE_BLOCK");
        blacklist.add("STRUCTURE_VOID");
        blacklist.add("JIGSAW");
        blacklist.add("SPAWNER");

        }
        List<Material> blacklistMaterials = new ArrayList<>();
        for (String material : blacklist) {
            try {
                blacklistMaterials.add(Material.valueOf(material));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in blacklist: " + material);
            }
        }
        ItemStack reward = new ItemStack(getRandomMaterial(blacklistMaterials));
        int slot = p.getInventory().firstEmpty();
        if (slot == -1) {
            p.getWorld().dropItem(p.getLocation(), reward);
        } else {
            p.getInventory().addItem(reward);
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {

            String itemName = reward.getType().name().toLowerCase().replace("_", " ");
            player.sendMessage(plugin.getLangManager().getFrom("daily.broadcast", p).replace("{player}", p.getName()).replace("{item}", itemName));
        }

        PlayerData data = plugin.getDataManager().getPlayerData(p);
        data.setLastClaim(System.currentTimeMillis());
        data.save();
    }

    private Material getRandomMaterial(List<Material> blacklist) {
        Material[] materials = Material.values();
        Random random = new Random();
        Material randomMaterial;

        do {
            randomMaterial = materials[random.nextInt(materials.length)];
        } while (!randomMaterial.isItem() || blacklist.contains(randomMaterial)); // Evita materiales no válidos o en la blacklist
        
        return randomMaterial;
    }
    public boolean canClaimDailyReward(Player p){
        PlayerData data = plugin.getDataManager().getPlayerData(p);
        long lastClaim = data.getLastClaim();
        long currentTime = System.currentTimeMillis();
        long cooldown = 86400000; // 24 horas en milisegundos

        if (currentTime - lastClaim < cooldown) {
            return false;
        }
        return true;
    }
    public String getText(Player p){
        PlayerData data = plugin.getDataManager().getPlayerData(p);
        long lastClaim = data.getLastClaim();
        long currentTime = System.currentTimeMillis();
        long cooldown = 86400000; // 24 horas en milisegundos

        if (currentTime - lastClaim < cooldown) {
            long remaining = cooldown - (currentTime - lastClaim);
            long hours = remaining / 3600000;
            long minutes = (remaining % 3600000) / 60000;
            long seconds = (remaining % 60000) / 1000;
            String left = plugin.getLangManager().getFrom("daily.time_left", p).replace("{hours}", String.valueOf(hours)).replace("{minutes}", String.valueOf(minutes)).replace("{seconds}", String.valueOf(seconds));
            return left;
        }
        return plugin.getLangManager().getFrom("daily.now", p);
    }
}
