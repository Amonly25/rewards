package com.ar.askgaming.rewards.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class RewardsCommands implements TabExecutor{

    private RewardsPlugin plugin;
    public RewardsCommands(RewardsPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0){
            plugin.getRewardsGui().openGui(p);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")){
            if (!sender.hasPermission("rewards.admin")){
                sender.sendMessage("§cYou do not have permission to use this command");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage("§aConfig reloaded");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
