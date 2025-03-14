package com.ar.askgaming.rewards.Listeners.PlayerListeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.ar.askgaming.rewards.RewardsPlugin;

import fr.xephi.authme.events.LoginEvent;

public class PlayerLoginListener implements Listener{

    private final RewardsPlugin plugin;
    public PlayerLoginListener(RewardsPlugin main) {
        plugin = main;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onLogin(LoginEvent e) {
        Player p = e.getPlayer();
        plugin.getReferrals().sendMessage(p);
    }

}
