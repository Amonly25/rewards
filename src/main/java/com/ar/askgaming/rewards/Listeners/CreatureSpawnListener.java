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

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Crates.Crate;

public class CreatureSpawnListener implements Listener{

    private RewardsPlugin plugin;
    public CreatureSpawnListener(RewardsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
                           
                            ItemStack item = plugin.getCrateManager().getCrateItem(crate);
                            Entity drop = entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), item);

                            if (entity.addPassenger(drop)){
                                //Bukkit.broadcastMessage(key + " " + chance + " " + random + entity.getLocation());
                                double health = ((Attributable) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                                int mutiplier = plugin.getConfig().getInt("crate_spawn_on_entity.modify_health_multiplier",3);
                                ((Attributable) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health * mutiplier);
                                ((Enemy)entity).setHealth(health * mutiplier);
                                break;
                            } else {
                                //Bukkit.broadcastMessage("§cCrate cant be added to entity");
                                drop.remove();
                            }
                        }
                    }
                }
            }
        }
    }
}
