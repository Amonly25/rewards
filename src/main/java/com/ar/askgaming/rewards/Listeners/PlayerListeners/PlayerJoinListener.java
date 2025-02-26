package com.ar.askgaming.rewards.Listeners.PlayerListeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ar.askgaming.rewards.RewardsPlugin;

public class PlayerJoinListener implements Listener{

    private RewardsPlugin plugin;

    public PlayerJoinListener(RewardsPlugin main) {
        plugin = main;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        
        Player p = e.getPlayer();

        plugin.getDatabaseManager().loadPlayerData(p.getUniqueId());
        plugin.getDataManager().convertData(p.getUniqueId());

        if (!p.hasPlayedBefore()){
            plugin.getReferrals().sendMessage(p);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (plugin.getDailyReward().canClaimDailyReward(p)){
                p.sendMessage(plugin.getLangManager().getFrom("daily.can_claim", p));
            }
            plugin.getStreakConnection().process(p);
            
            if (plugin.getServer().getPluginManager().getPlugin("VotifierPlus") != null) {
                plugin.getVoteReward().checkOnJoin(p);
            }

        }, 100);

    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        plugin.getPlaytimeManager().updatePlaytime(p);
    }
    
}
