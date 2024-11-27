package com.ar.askgaming.rewards.Commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.rewards.Crate;
import com.ar.askgaming.rewards.RewardsPlugin;

import net.md_5.bungee.api.ChatColor;

public class CrateCommands implements TabExecutor {

    private RewardsPlugin plugin;
    public CrateCommands(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            return false;
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
        
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "set", "menu");
        } else {
            if (args[0].equalsIgnoreCase("set")) {
                return setValue;
            }

        }
        return null;
    }
    //#region create
    public void createCommand(Player p, String[] args){
        if (args.length != 2) {
            p.sendMessage("Usage: crate create <name>");
            return;
        }
        String name = args[1];
        if (plugin.getCrateManager().getCrates().containsKey(name)){
            p.sendMessage("Esa caja con ese nombre ya existe");
            return;
        }

        plugin.getCrateManager().createCrate(name, null);

    }
    //#region delete
    public void deleteCommand(Player p, String[] args){
        if (args.length != 2) {
            p.sendMessage("Usage: crate delete <name>");
            return;
        }
        String name = args[1];
        if (!plugin.getCrateManager().getCrates().containsKey(name)){
            p.sendMessage("Esa caja no existe");
            return;
        }
        plugin.getCrateManager().deleteCrate(name);
    }
    //#region set
    public void setCommand(Player p, String[] args){
        if (args.length < 3) {
            p.sendMessage("Usage: crate set <name> <key> <value>");
            return;
        }
        String name = args[1];
        if (!plugin.getCrateManager().getCrates().containsKey(name)){
            p.sendMessage("Esa caja no existe");
            return;
        }
        Crate crate = plugin.getCrateManager().getCrates().get(name);
        String key = args[2];
        if (!setValue.contains(key)){
            p.sendMessage("Esa key no es valida");
            return;
        }
        String value = args[3];
        switch (key) {
            case "cost":
                try {
                    double cost = Double.parseDouble(value);
                    crate.setOpenCost(cost);
                    p.sendMessage("Cost set to "+cost);
                } catch (Exception e) {
                    p.sendMessage("El valor debe ser un numero");
                }
                break;
            case "block":
                Set<Material> transparentMaterials = new HashSet<>(Arrays.asList(Material.AIR, Material.WATER));
                Block targetBlock = p.getTargetBlock(transparentMaterials, 5);
                if (transparentMaterials.contains(targetBlock.getType())) {
                    return;
                }
                crate.setBlockLinked(targetBlock);
                p.sendMessage("Block linked set to "+targetBlock.getType());
                break;
            case "rewards":
                ItemStack[] rewards = p.getInventory().getContents();
                crate.setRewards(rewards);
                p.sendMessage("Rewards set from your inventory");
                break;    
            case "displayname":
                crate.setDisplayName(ChatColor.translateAlternateColorCodes('&', value));
                p.sendMessage("Display name set to "+value);
                break;    
            case "openfrominventory":
                try {
                    boolean openFromInventory = Boolean.parseBoolean(value);
                    crate.setOpenFromInventory(openFromInventory);
                    p.sendMessage("Open from inventory set to "+openFromInventory);
                } catch (Exception e) {
                    p.sendMessage("El valor debe ser un booleano");
                }
                break;   
            case "openbyblock":
                if (crate.getBlockLinked() == null){
                    p.sendMessage("No hay block linked");
                    return;
                }
                try {
                    boolean openByBlock = Boolean.parseBoolean(value);
                    crate.setOpenByBlock(openByBlock);
                    p.sendMessage("Open by block set to "+openByBlock);
                } catch (Exception e) {
                    p.sendMessage("El valor debe ser un booleano");
                }
                break;
            case "textdisplay":
                TextDisplay text = crate.getTextDisplay();
                if (crate.getBlockLinked() == null){
                    p.sendMessage("No hay block linked");
                    return;

                }
                if (text == null){
                    p.sendMessage("No hay text display");
                    return;
                }
                
                crate.getTextDisplay().setText(ChatColor.translateAlternateColorCodes('&', value));
                p.sendMessage("Text display set to "+value);
                break;     
            case "keyitem":
                ItemStack keyItem = p.getInventory().getItemInMainHand();
                if (keyItem == null || keyItem.getType().isAir()){
                    p.sendMessage("No hay item en la mano");
                    return;
                }
                crate.setKeyItem(keyItem);
                p.sendMessage("Key item set to "+keyItem.getType());
                break;
            case "createitem":
                ItemStack createItem = p.getInventory().getItemInMainHand();
                if (createItem == null || createItem.getType().isAir()){
                    p.sendMessage("No hay item en la mano");
                    return;
                }
                crate.setCrateItem(createItem);
                p.sendMessage("Crate item set to "+createItem.getType());
                break;
            case "keyrequerid":
                try {
                    boolean keyRequired = Boolean.parseBoolean(value);
                    crate.setKeyRequired(keyRequired);
                    p.sendMessage("Key required set to "+keyRequired);
                } catch (Exception e) {
                    p.sendMessage("El valor debe ser un booleano");
                }
                break;
            case "broadcastReward":
                try {
                    boolean broadcastReward = Boolean.parseBoolean(value);
                    crate.setBroadcastReward(broadcastReward);
                    p.sendMessage("Broadcast reward set to "+broadcastReward);
                } catch (Exception e) {
                    p.sendMessage("El valor debe ser un booleano");
                }
                break;
                
            default:
                break;
        }

    }
    private List<String> setValue = List.of("keyrequerid","broadcastReward", "cost", "block", "rewards", "displayname", "openfrominventory", "openbyblock", "textdisplay", "keyitem","createitem");
    
}
