package com.ar.askgaming.rewards.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ar.askgaming.rewards.RewardsPlugin;

public class PlayerJoinListener implements Listener{

    private RewardsPlugin plugin;

    public PlayerJoinListener(RewardsPlugin main) {
        plugin = main;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()){
            //Bukkit.broadcastMessage("First join!");
        }
    }
    
}
