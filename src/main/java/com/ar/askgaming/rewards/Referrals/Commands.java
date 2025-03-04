package com.ar.askgaming.rewards.Referrals;

import java.util.ArrayList;
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
    private String getLang(String key, Player player) {
        return plugin.getLangManager().getFrom(key, player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(List.of("getcode", "list")); // Lista modificable
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
        
        if (args.length == 0) {
            sender.sendMessage("Usage: /referral help for more information");
            return true;
        }

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
            case "help":
                String text = getLang("referral.help", (sender instanceof Player) ? (Player) sender : null);
                sender.sendMessage(text);
                break;
            default:
                addReferral(sender, args);
                break;
        }
        return true;
    }
    //#region addReferral
    private void addReferral(CommandSender sender, String[] args) {
        if (args.length != 1) {
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
            sender.sendMessage(getLang("referral.already_referred", player));
            return;
        }
        if (code.equalsIgnoreCase(data.getReferralCode())) {
            sender.sendMessage(getLang("referral.already_referred", player));
            return;

        }

        if (plugin.getReferrals().getReferredBy(code) == null) {
            sender.sendMessage(getLang("referral.code_not_found", player));
            return;
        }
        if (plugin.getConfig().getBoolean("referral.no_count_old_players")) {
            int playtime = plugin.getPlaytimeManager().getPlaytimeMinutes(player);
            if (playtime >= 60) {
                sender.sendMessage(getLang("referral.cant_use", player));
                return;
            }
        }

        plugin.getReferrals().onReferral(player, code);
    }
    //#region getCode
    private void getCode(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command");
            return;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());

            String code = pData.getReferralCode();
            if (code == null || code.isBlank()) {
                manager.createReferralCode(player);
                player.sendMessage(getLang("referral.creating_code", player));
                return;
            }
            sender.sendMessage(getLang("referral.code", player).replace("{code}", code));
            return;
        }
        String playerName = args[1];
        String code = manager.getReferralCode(playerName);
        sender.sendMessage(getLang("referral.his_code", player).replace("{player}", playerName).replace("{code}", code));
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

        if (plugin.getReferrals().getReferredBy(args[2]) == null) {
            sender.sendMessage("§cCode does not exist");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found, make sure they are online");
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
                sender.sendMessage(getLang("referral.code_not_found", player));
                return;
            }
            String size = String.valueOf(pData.getReferredPlayers().size());
            String players = String.join(", ", pData.getReferredPlayers());
            sender.sendMessage(getLang("referral.code", player).replace("{code}", pData.getReferralCode()));
            sender.sendMessage(getLang("referral.player_referred", player).replace("{size}", size).replace("{players}", players));
         
            return;
        }
        if (!sender.hasPermission("rewards.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command");
            return;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
        RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(target.getUniqueId());
        if (pData == null) {
            sender.sendMessage("§cPlayer not found");
            return;
        }
        if (pData.getReferralCode() == null || pData.getReferralCode().isBlank()) {
            sender.sendMessage("§cPlayer does not have a referral code");
            return;
        }
        sender.sendMessage("§7Referral code for " + target.getName() + ": " + pData.getReferralCode());
        sender.sendMessage("§7Referred players: " + String.join(", ", pData.getReferredPlayers()));
        
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
            sender.sendMessage("§cAmount must be an integer");
            return;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
        RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(target.getUniqueId());
        if (pData == null) {
            sender.sendMessage("§cPlayer not found");
            return;
        }
        sender.sendMessage("Processing add referral for " + target.getName() + " with amount " + amount);
        plugin.getReferrals().addCommand(target, amount);
    }


}
