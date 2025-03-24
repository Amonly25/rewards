package com.ar.askgaming.rewards.Vote;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.RewardsPlayerData;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener{

    private RewardsPlugin plugin;
    public VoteListener(RewardsPlugin plugin){
        this.plugin = plugin;
        new Commands(plugin);

        if (plugin.getServer().getPluginManager().getPlugin("VotifierPlus") != null) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private HashMap<String, Integer> queueVotes = new HashMap<>();

    @EventHandler
    public void onVote(VotifierEvent e){

        String playerName = e.getVote().getUsername();

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            queueVotes.put(playerName, queueVotes.getOrDefault(playerName, 0) + 1);
            return;
        }
        giveRewards(playerName);
    }
    public void giveRewards(String playerName){

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return;
        RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        pData.setVotes(pData.getVotes() + 1);
        pData.save();

        FileConfiguration config = plugin.getConfig();
        for (String key : config.getConfigurationSection("vote.rewards").getKeys(false)) {
                                
            String message = config.getString("vote.rewards." + key + ".message");
            String broadcast = config.getString("vote.rewards." + key + ".broadcast");
            List<String> commands = config.getStringList("vote.rewards." + key + ".commands");
            double chance = config.getDouble("vote.rewards." + key + ".chance");
            
            if (Math.random() * 100 < chance) {
                if (!message.equals("")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
                if (!broadcast.equals("")) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast).replace("%player%", player.getName()));
                }
            
                for (String s : commands){
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()));
                }
            }
        }
    }
    public void checkOnJoin(Player player){
        int votes = queueVotes.getOrDefault(player.getName(), 0);
        if (votes > 0) {
            for (int i = 0; i < votes; i++) {
                giveRewards(player.getName());
            }
            queueVotes.remove(player.getName());
        }
    }

}
