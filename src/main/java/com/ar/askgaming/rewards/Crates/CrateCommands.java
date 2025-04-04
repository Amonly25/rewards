package com.ar.askgaming.rewards.Crates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.RewardsPlugin;

import net.md_5.bungee.api.ChatColor;

public class CrateCommands implements TabExecutor {

    private final RewardsPlugin plugin;
    private final CrateManager manager;
    private final Set<String> setValue = Set.of("key_requerid", "broadcast_reward", "block", "rewards", "remove_block", "open_from_inventory", "open_by_block", "text_display", "key_item", "crate_item");
    
    public CrateCommands(CrateManager manager){
        this.plugin = RewardsPlugin.getInstance();
        this.manager = manager;

        plugin.getServer().getPluginCommand("crate").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /crate <create/delete/set/menu/give/preview>");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            giveCommand(sender, args);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        Player p = (Player) sender;

        if (args[0].equalsIgnoreCase("preview")) {
            previewCommand(p, args);
            return true;
        }
        if (!p.hasPermission("rewards.crate.admin")){
            p.sendMessage("§cYou dont have permission to use this command.");
            return true;
        }

            switch (args[0].toLowerCase()) {
                case "create" -> createCommand(p, args);
                case "delete" -> deleteCommand(p, args);
                case "set" -> setCommand(p, args);
                case "menu" -> p.openInventory(manager.getGui());
            default -> p.sendMessage("§cUsage: /crate <create/delete/set/menu>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                List<String> commands = new ArrayList<>(Arrays.asList("preview"));
                if (sender.hasPermission("rewards.crate.admin")) {
                    commands.addAll(Arrays.asList("create", "delete", "set", "menu", "give"));
                }
                return commands;
            case 2:
                return new ArrayList<>(manager.getCrates().keySet());
            case 3:
                return new ArrayList<>(setValue);
            default:
                return null;
        }
    }
    //#region create
    public void createCommand(Player p, String[] args){
        if (args.length != 2) {
            p.sendMessage("§cUsage: crate create <name>");
            return;
        }
        String name = args[1].toLowerCase();
        if (manager.getCrates().containsKey(name)){
            p.sendMessage("§cThis crate already exists.");
            return;
        }
        p.sendMessage("§aCrate " + name + " created.");
        manager.createCrate(name, null);

    }
    //#region delete
    public void deleteCommand(Player p, String[] args){
        if (args.length != 2) {
            p.sendMessage("§cUsage: crate delete <name>");
            return;
        }
        String name = args[1].toLowerCase();
        if (!manager.getCrates().containsKey(name)){
            p.sendMessage("§cThis crate doesnt exists.");
            return;
        }
        manager.deleteCrate(name);
        p.sendMessage("§aCrate " + name + " deleted.");
    }
    //#region set
    public void setCommand(Player p, String[] args){
        if (args.length < 3) {
            p.sendMessage("§cUsage: crate set <name> <key> <value>");
            return;
        }
        String name = args[1].toLowerCase();
        Crate crate = manager.getCrates().get(name);
        if (crate == null){
            p.sendMessage("§cThis crate doesnt exists.");
            return;
        }

        String key = args[2];
        if (!setValue.contains(key)){
            p.sendMessage("§cThis key is not valid.");
            return;
        }
        if (args.length == 3) {
            handleThreeArgs(p, crate, key);
            return;
        } 
        if (args.length == 4) {
            handleFourArgs(p, crate, key, args);
            return;
        } 
        if (key.equals("text_display")) {
            setTextDisplay(p, crate, args);
        } else {
            p.sendMessage("§cUsage: crate set <name> <key> <value>");
        }
    }
        
    //#region handleThreeArgs
    private void handleThreeArgs(Player p, Crate crate, String key) {
        switch (key) {
            case "rewards":
                if (manager.getEditing().containsKey(crate.getName())) {
                    p.sendMessage(crate.getName() + " is already being edited.");
                    return;
                }
                ItemStack[] rewards = crate.getRewards();
                Inventory inv = Bukkit.createInventory(null, 27, "§6Updated rewards, close to save.");
                inv.setContents(rewards);
                manager.getEditing().put(crate.getName(), inv);
                p.openInventory(inv);
                break;
            case "block":
                Set<Material> transparentMaterials = new HashSet<>(Arrays.asList(Material.AIR, Material.WATER));
                Block targetBlock = p.getTargetBlock(transparentMaterials, 5);
                if (transparentMaterials.contains(targetBlock.getType())) {
                    return;
                }
                crate.setBlockLinked(targetBlock);
                crate.setOpenByBlock(true);
                crate.setKeyRequired(true);
                p.sendMessage("§aBlock linked set to " + targetBlock.getType());
                p.sendMessage("§6Open by block and key requerid set to true");
                manager.save(crate);
                break;    
            case "remove_block":
                TextDisplay textDisplay = crate.getTextDisplay();
                if (textDisplay != null) {
                    textDisplay.remove();
                }
                p.sendMessage("§aBlock linked removed");
                manager.save(crate);
                crate.setBlockLinked(null);
                break;
            case "key_item":
                setItemInHand(p, crate::setKeyItem, "§6Key item set to ",crate);
                break;
            case "crate_item":
                setItemInHand(p, crate::setCrateItem, "§6Crate item set to ",crate);
                break;
            default:
                p.sendMessage("§cThis key is not valid.");
                break;
        }
    }
    //#region handleFourArgs
    private void handleFourArgs(Player p, Crate crate, String key, String[] args) {
        switch (key) {
            case "cost":
                setCost(p, crate, args[3]);
                break;
            case "open_from_inventory":
                setBooleanValue(p, crate::setOpenFromInventory, args[3], "§6Open from inventory set to ", crate);
                break;
            case "open_by_block":
                if (crate.getBlockLinked() == null) {
                    p.sendMessage("§cNo block linked");
                    return;
                }
                setBooleanValue(p, crate::setOpenByBlock, args[3], "§6Open by block set to ",crate);
                break;
            case "text_display":
                setTextDisplay(p, crate, args);
                break;
            case "key_requerid":
                setBooleanValue(p, crate::setKeyRequired, args[3], "§6Key required set to ",crate);
                break;
            case "broadcast_reward":
                setBooleanValue(p, crate::setBroadcastReward, args[3], "§6Broadcast reward set to ",crate);
                break;
            default:
                p.sendMessage("§cThis key is not valid.");
                break;
        }
    }
    
    private void setCost(Player p, Crate crate, String value) {
        try {
            double cost = Double.parseDouble(value);
            crate.setOpenCost(cost);
            p.sendMessage("§6Cost set to " + cost);
            manager.save(crate);
        } catch (NumberFormatException e) {
            p.sendMessage("§cThe value must be a number.");
        }
    }
    
    private void setBooleanValue(Player p, Consumer<Boolean> setter, String value, String message,Crate crate) {
        try {
            boolean boolValue = Boolean.parseBoolean(value);
            setter.accept(boolValue);
            p.sendMessage(message + boolValue);
            manager.save(crate);
        } catch (Exception e) {
            p.sendMessage("§cThe value must be a boolean.");
        }
    }
    //#region setTextDisplay
    private void setTextDisplay(Player p, Crate crate, String[] args) {

        TextDisplay text = crate.getTextDisplay();
        
        if (crate.getBlockLinked() == null) {
            p.sendMessage("§cNo block linked");
            return;
        }
        if (text == null) {
            p.sendMessage("§cNo text display set");
            return;
        }

        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        value = value.replace("\\n", "\n");
        text.setText(ChatColor.translateAlternateColorCodes('&', value));
        crate.setDisplayText(value);

        manager.save(crate);
        p.sendMessage("§6Text display set to " + value);
    }
    
    private void setItemInHand(Player p, Consumer<ItemStack> setter, String message, Crate crate) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            p.sendMessage("§cYou must be holding an item.");
            return;
        }
        setter.accept(item);
        manager.save(crate);
        p.sendMessage(message + item.getType());
    }
    //#region give
    private void giveCommand(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage("§cUsage: crate give <name> <player> <crate/key>");
            return;
        }
        String name = args[1].toLowerCase();
        Crate crate = manager.getCrates().get(name);
        if (crate == null){
            sender.sendMessage("§cThis crate doesnt exists.");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }
        if (args[3].equalsIgnoreCase("key")) {
            target.getInventory().addItem(crate.getKeyItem());
            sender.sendMessage("§6Key given to " + target.getName());
            return;
        } else if (!args[3].equalsIgnoreCase("crate")) {
            sender.sendMessage("§cUsage: crate give <name> <player> <crate/key>");
            return;
        }
        sender.sendMessage("§6Crate given to " + target.getName());
        int slot = target.getInventory().firstEmpty();
        if (slot == -1) {
            target.getWorld().dropItem(target.getLocation(), crate.getCrateItem());
            return;
        }
        target.getInventory().addItem(crate.getCrateItem());
    }
    //#region preview
    private void previewCommand(Player p, String[] args) {
        if (args.length != 2) {
            p.sendMessage("§cUsage: crate preview <name>");
            return;
        }
        String name = args[1].toLowerCase();
        Crate crate = manager.getCrates().get(name);
        if (crate == null){
            p.sendMessage("§cThis crate doesnt exists.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, "§6Preview " + name);
        manager.getEditing().put(crate.getName(), inv);
        inv.setContents(crate.getRewards());
        p.openInventory(inv);
    }
}
