package com.ar.askgaming.rewards.Listeners;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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

    private final RewardsPlugin plugin;
    public CreatureSpawnListener(RewardsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == SpawnReason.NATURAL) {
            Entity entity = e.getEntity();
            if (entity instanceof Enemy) {
                
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("crate_spawn_on_entity.crates_chance");
                if (section == null) {
                    return; 
                }

                for (String key : section.getKeys(false)) {
                    Crate crate = plugin.getCrateManager().getCrateByName(key);
                    if (crate == null) {
                        plugin.getLogger().warning("Crate " + key + " not found in CrateManager.");
                        continue;
                    }

                    double chance = section.getDouble(key);
                    double random = Math.random() * 100;

                    if (random <= chance) {
                        ItemStack item = plugin.getCrateManager().getCrateItem(crate);
                        Entity drop = entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), item);
                        

                        if (entity.addPassenger(drop)) {
                            double health = 20.0; // Valor base por defecto.
                            if (entity instanceof Attributable) {
                                Attributable attributable = (Attributable) entity;
                                AttributeInstance maxHealth = attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                                if (maxHealth != null) {
                                    health = maxHealth.getBaseValue();
                                    int mutiplier = plugin.getConfig().getInt("crate_spawn_on_entity.modify_health_multiplier", 3);
                                    maxHealth.setBaseValue(health * mutiplier);
                                    ((Enemy) entity).setHealth(health * mutiplier);
                                }
                            }
                            break;
                        } else {
                            plugin.getLogger().warning("Crate could not be added as a passenger.");
                            drop.remove();
                        }
                    }
                }
            }
        }
    }
}
