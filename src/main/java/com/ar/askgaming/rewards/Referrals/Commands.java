package com.ar.askgaming.rewards.Referrals;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.RewardsPlayerData;

public class Commands implements TabExecutor{

    private ReferralsManager manager;
    private RewardsPlugin plugin;

    public Commands(RewardsPlugin plugin, ReferralsManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        plugin.getServer().getPluginCommand("referral").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("getcode");
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        switch (args[0].toLowerCase()) {
            case "getcode":
                getCode(sender, args);
                break;
        
            default:
                break;
        }
        return true;
    }
    private void getCode(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to use this command");
                return;
            }
            Player player = (Player) sender;
            RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
            String code = pData.getReferralCode();
            if (code == null || code.isBlank()) {
                manager.createReferralCode(player);
                code = manager.getRefferalCode(player);
            }
            sender.sendMessage("Your referral code is: " + code);
            return;
        }
        String playerName = args[1];
        sender.sendMessage("Referral code for " + playerName + ": " + manager.getReferralCode(playerName));
    }


}
