package com.ar.askgaming.rewards.Listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.Crate;
import com.ar.askgaming.rewards.RewardsPlugin;

public class CreatureSpawnListener implements Listener{

    private RewardsPlugin plugin;
    public CreatureSpawnListener(RewardsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == SpawnReason.NATURAL) {
            Entity entity = e.getEntity();
            if (entity instanceof Enemy){

                ConfigurationSection section = plugin.getConfig().getConfigurationSection("crate_spawn_on_entity.crates_chance");

                for (String key : section.getKeys(false)) {

                    Crate crate = plugin.getCrateManager().getCrateByName(key);
                    if (crate != null) {

                        double chance = section.getDouble(key);
                        double random = Math.random() * 100;
                        
                        if (random <= chance) {
                            Bukkit.broadcastMessage(key + " " + chance + " " + random + entity.getLocation());
                            ItemStack item = plugin.getCrateManager().getCrateItem(crate);
                            Entity drop = entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), item);
                            entity.addPassenger(drop);

                            double health = ((Attributable) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                            int mutiplier = plugin.getConfig().getInt("crate_spawn_on_entity.health_multiplier");
                            ((Attributable) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health * mutiplier);
                            ((Enemy)entity).setHealth(health * mutiplier);
                            break;
                        }
                    } else {
                        plugin.getLogger().warning("Crate " + key + " not found!");
                    }
                }
            }
        }
    }
}
