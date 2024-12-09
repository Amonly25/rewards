package com.ar.askgaming.rewards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class RewardsManager {

    private RewardsPlugin plugin;

    public RewardsManager(RewardsPlugin main) {
        plugin = main;
    }

    public void onFirstJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            //This will only works on premium servers
            
            //Bukkit.broadcastMessage("First join!");
        }
    }

}
