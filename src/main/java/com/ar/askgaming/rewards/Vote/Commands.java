package com.ar.askgaming.rewards.Vote;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class Commands implements CommandExecutor{

    private RewardsPlugin plugin;
    public Commands(RewardsPlugin plugin){
        this.plugin = plugin;

        plugin.getServer().getPluginCommand("vote").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            Player p = (Player) sender;
            p.sendMessage(plugin.getLangManager().getFrom("vote", p));
            return true;
        }
        return false;
    }

}
