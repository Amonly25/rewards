package com.ar.askgaming.rewards.Playtime;

import java.util.Collections;
import java.util.List;

import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

public class Commands implements TabExecutor{

    private RewardsPlugin plugin;
    public Commands(RewardsPlugin plugin){
        this.plugin = plugin;

        plugin.getServer().getPluginCommand("playtime").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0){
            int total_minutes = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
            int hours = total_minutes / 60;
            int minutes = total_minutes % 60;

            p.sendMessage(plugin.getLangManager().getFrom("playtime.get", p).replace("{hours}", hours + "").replace("{minutes}", minutes + ""));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "top":
                plugin.getPlaytimeManager().sendTop10(p);
                return true;
            case "compare_now":
                if (!sender.hasPermission("rewards.admin")){
                    sender.sendMessage("§cYou do not have permission to use this command");
                    return true;
                }
                plugin.getPlaytimeManager().compareNow();
                return true;
            case "reset":
                if (args.length == 2) {
                    resetPlaytime(sender, args[1]);
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }
    private void resetPlaytime(CommandSender sender, String targetName) {
        if (!sender.hasPermission("rewards.admin")){
            sender.sendMessage("§cYou do not have permission to use this command");
            return;
        }
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return;
        }
        target.setStatistic(Statistic.PLAY_ONE_MINUTE, 0);
        sender.sendMessage("§2Playtime reset to " + target.getName());
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("top", "compare_now", "reset");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return null; // Return player names
        }
        return Collections.emptyList();
    }

}
