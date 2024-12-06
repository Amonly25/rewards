package com.ar.askgaming.rewards.Commands;

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

import com.ar.askgaming.rewards.Crate;
import com.ar.askgaming.rewards.RewardsPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CrateCommands implements TabExecutor {

    private RewardsPlugin plugin;
    public CrateCommands(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("Usage: /crate <create/delete/set/menu>");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }
        Player p = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "create":
                createCommand(p,args);
                break;
            case "delete":
                deleteCommand(p, args); 
                break;
            case "set":
                setCommand(p, args);
                break;
            case "menu":
                p.openInventory(plugin.getCrateManager().getGui());
                break;

            default:
                p.sendMessage("Usage: /crate <create/delete/set/menu>");
                break;
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "set", "menu");
        } 
        if (args.length == 2){
            return plugin.getCrateManager().getCrates().keySet().stream().toList();
        }
        if (args.length == 3) {
            return setValue;
        }
        return null;
    }
    //#region create
    public void createCommand(Player p, String[] args){
        if (args.length != 2) {
            p.sendMessage("Usage: crate create <name>");
            return;
        }
        String name = args[1].toLowerCase();
        if (plugin.getCrateManager().getCrates().containsKey(name)){
            p.sendMessage("Esa caja con ese nombre ya existe");
            return;
        }
        p.sendMessage("Has creado la caja "+name);
        plugin.getCrateManager().createCrate(name, null);

    }
    //#region delete
    public void deleteCommand(Player p, String[] args){
        if (args.length != 2) {
            p.sendMessage("Usage: crate delete <name>");
            return;
        }
        String name = args[1].toLowerCase();
        if (!plugin.getCrateManager().getCrates().containsKey(name)){
            p.sendMessage("Esa caja no existe");
            return;
        }
        plugin.getCrateManager().deleteCrate(name);
        p.sendMessage("Has eliminado la caja "+name);
    }
    //#region set
    public void setCommand(Player p, String[] args){
        if (args.length < 2) {
            p.sendMessage("Usage: crate set <name> <key> <value>");
            return;
        }
        String name = args[1].toLowerCase();
        Crate crate = plugin.getCrateManager().getCrates().get(name);
        if (crate == null){
            p.sendMessage("Esa caja no existe");
            return;
        }

        String key = args[2];
        if (!setValue.contains(key)){
            p.sendMessage("Esa key no es valida");
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
        if (key.equals("textdisplay")) {
            Bukkit.broadcastMessage("test");
            setTextDisplay(p, crate, args);
        } else {
            p.sendMessage("Usage: crate set <name> <key> <value>");
        }
    }
    private List<String> setValue = List.of("keyrequerid","broadcastReward", "cost", "block", "rewards", "removeblock", "openfrominventory", "openbyblock", "textdisplay", "keyitem","crateitem");
    
    //#region handleThreeArgs
    private void handleThreeArgs(Player p, Crate crate, String key) {
        switch (key) {
            case "rewards":
                if (plugin.getCrateManager().getEditing().containsKey(crate)) {
                    p.sendMessage(crate.getName() + " ya esta siendo editada");
                    return;
                }
                ItemStack[] rewards = crate.getRewards();
                Inventory inv = Bukkit.createInventory(null, 27, "Updated rewards, close to save.");
                inv.setContents(rewards);
                plugin.getCrateManager().getEditing().put(crate, inv);
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
                p.sendMessage("Block linked set to " + targetBlock.getType());
                plugin.getCrateManager().save();
                break;    
            case "removeblock":
                TextDisplay textDisplay = crate.getTextDisplay();
                if (textDisplay != null) {
                    textDisplay.remove();
                }
                p.sendMessage("Block linked removed");
                plugin.getCrateManager().save();
                crate.setBlockLinked(null);
                break;
            case "keyitem":
                setItemInHand(p, crate::setKeyItem, "Key item set to ");
                break;
            case "crateitem":
                setItemInHand(p, crate::setCrateItem, "Crate item set to ");
                break;
            default:
                p.sendMessage("Esa key no es valida");
                break;
        }
    }
    //#region handleFourArgs
    private void handleFourArgs(Player p, Crate crate, String key, String[] args) {
        switch (key) {
            case "cost":
                setCost(p, crate, args[3]);
                break;
            case "openfrominventory":
                setBooleanValue(p, crate::setOpenFromInventory, args[3], "Open from inventory set to ");
                break;
            case "openbyblock":
                if (crate.getBlockLinked() == null) {
                    p.sendMessage("No hay block linked");
                    return;
                }
                setBooleanValue(p, crate::setOpenByBlock, args[3], "Open by block set to ");
                break;
            case "textdisplay":
                setTextDisplay(p, crate, args);
                break;
            case "keyrequerid":
                setBooleanValue(p, crate::setKeyRequired, args[3], "Key required set to ");
                break;
            case "broadcastReward":
                setBooleanValue(p, crate::setBroadcastReward, args[3], "Broadcast reward set to ");
                break;
            default:
                p.sendMessage("Esa key no es valida");
                break;
        }
    }
    
    private void setCost(Player p, Crate crate, String value) {
        try {
            double cost = Double.parseDouble(value);
            crate.setOpenCost(cost);
            p.sendMessage("Cost set to " + cost);
            plugin.getCrateManager().save();
        } catch (NumberFormatException e) {
            p.sendMessage("El valor debe ser un numero");
        }
    }
    
    private void setBooleanValue(Player p, Consumer<Boolean> setter, String value, String message) {
        try {
            boolean boolValue = Boolean.parseBoolean(value);
            setter.accept(boolValue);
            p.sendMessage(message + boolValue);
            plugin.getCrateManager().save();
        } catch (Exception e) {
            p.sendMessage("El valor debe ser un booleano");
        }
    }
    //#region setTextDisplay
    private void setTextDisplay(Player p, Crate crate, String[] args) {

        TextDisplay text = crate.getTextDisplay();
        
        if (crate.getBlockLinked() == null) {
            p.sendMessage("No hay block linked");
            return;
        }
        if (text == null) {
            p.sendMessage("No hay text display");
            return;
        }

        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        text.setText(ChatColor.translateAlternateColorCodes('&', value));
        crate.setDisplayText(value);
        plugin.getCrateManager().save();
        p.sendMessage("Text display set to " + value);
    }
    
    private void setItemInHand(Player p, Consumer<ItemStack> setter, String message) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            p.sendMessage("No hay item en la mano");
            return;
        }
        setter.accept(item);
        plugin.getCrateManager().save();
        p.sendMessage(message + item.getType());
    }
}
