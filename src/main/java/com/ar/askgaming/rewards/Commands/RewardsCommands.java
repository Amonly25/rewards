package com.ar.askgaming.rewards.Commands;

import java.util.List;

import org.bukkit.Statistic;
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
        if (args.length == 1){
            switch (args[0].toLowerCase()) {
                case "playtime":
                    int total_minutes = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
                    int hours = total_minutes / 60;
                    int minutes = total_minutes % 60;
                    p.sendMessage("You have played for " + hours + " hours and " + minutes + " minutes");
                    return true;
            
                default:
                    return true;
            }
        }
        if (args.length == 2){
            switch (args[0].toLowerCase()) {
                case "reset_playtime":
                    if (!p.hasPermission("rewards.admin")){
                        p.sendMessage("You do not have permission to use this command");
                        return true;
                    }
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null){
                        p.sendMessage("Player not found");
                        return true;
                    }
                    target.setStatistic(Statistic.PLAY_ONE_MINUTE, 0);  
                    p.sendMessage("Playtime reset for " + target.getName());
                    return true;
           
                default:
                    return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

}
