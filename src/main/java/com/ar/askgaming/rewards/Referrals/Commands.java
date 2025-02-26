package com.ar.askgaming.rewards.Referrals;

import java.util.List;

import org.bukkit.OfflinePlayer;
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
            List<String> list = List.of("getcode", "list");
            if (sender.hasPermission("rewards.admin")) {
                list.add("add");
                list.add("addbuy");
            }
            return list;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        switch (args[0].toLowerCase()) {
            case "getcode":
                getCode(sender, args);
                break;
            case "list":
                getList(sender, args);
                break;
            case "add":
                add(sender, args);
                break;
            case "addbuy":
                addBuy(sender, args);
                break;
            default:
                addReferral(sender, args);
                break;
        }
        return true;
    }
    private void addReferral(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /referral <code>");
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command");
            return;
        }
        Player player = (Player) sender;
        String code = args[0];
        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        if (data.getReferredBy() != null && !data.getReferredBy().isEmpty()) {
            sender.sendMessage("You already claimed a referral.");
            return;
        }

        if (!plugin.getReferrals().existsCode(code)) {
            sender.sendMessage("Code not found.");
            return;
        }
        if (plugin.getConfig().getBoolean("referral.no_count_old_players")) {
            int playtime = plugin.getPlaytimeManager().getPlaytimeMinutes(player);
            if (playtime >= 60) {
                sender.sendMessage("You have played for more than 1 hour, you cannot claim a referral.");
                return;
            }
        }

        plugin.getReferrals().onReferral(player, code);
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
    //#region Add
    private void add(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /referral add <player> <code>" );
            return;
        }
        if (!sender.hasPermission("rewards.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command");
            return;
        }

        if (!plugin.getReferrals().existsCode(args[2])) {
            sender.sendMessage("Code does not exist");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found, make sure they are online");
            return;
        }
        
        plugin.getReferrals().onReferral(target, args[2]);
    }
    //#region List
    @SuppressWarnings("deprecation")
    private void getList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command");
            return;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
            if (pData.getReferralCode() == null || pData.getReferralCode().isBlank()) {
                sender.sendMessage("You do not have a referral code, use /referral getcode to get one");
                return;
            }
            sender.sendMessage("Your referral code is: " + pData.getReferralCode());
            sender.sendMessage("You have referred " + pData.getReferredPlayers().size() + " players: " + 
                String.join(", ", pData.getReferredPlayers()));
         
            return;
        }
        if (!sender.hasPermission("rewards.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command");
            return;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
        RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(target.getUniqueId());
        if (pData == null) {
            sender.sendMessage("Player not found");
            return;
        }
        if (pData.getReferralCode() == null || pData.getReferralCode().isBlank()) {
            sender.sendMessage("Player does not have a referral code");
            return;
        }
        sender.sendMessage("Referral code for " + target.getName() + ": " + pData.getReferralCode());
        sender.sendMessage("Referred players: " + String.join(", ", pData.getReferredPlayers()));
        
    }
    //#region Add Buy
    @SuppressWarnings("deprecation")
    private void addBuy(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /referral add <player> <amount>");
            return;
        }
        if (!sender.hasPermission("rewards.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Amount must be an integer");
            return;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
        RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(target.getUniqueId());
        if (pData == null) {
            sender.sendMessage("Player not found");
            return;
        }
        sender.sendMessage("Processing add referral for " + target.getName() + " with amount " + amount);
        plugin.getReferrals().addCommand(target, amount);
    }


}
