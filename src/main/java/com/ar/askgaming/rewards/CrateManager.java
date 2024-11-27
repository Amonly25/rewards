package com.ar.askgaming.rewards;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class CrateManager {

    private RewardsPlugin plugin;
    private File file;
    private FileConfiguration config;
    private Inventory gui;

    public Inventory getGui() {
        return gui;
    }

    private NamespacedKey key = new NamespacedKey(plugin, "ask_crate");

    public NamespacedKey getKey() {
        return key;
    }
    public CrateManager(RewardsPlugin plugin) {
        this.plugin = plugin;

        //Create File and load config
        file = new File(plugin.getDataFolder(), "crates.yml");
        if (!file.exists()) {
            plugin.saveResource("crates.yml", false);
        }
        config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Load crates from config
        for (String key : config.getKeys(false)) {
            if (config.get(key) instanceof Crate) {
                Crate crate = (Crate) config.get(key);
                crates.put(key, crate);
            }
        }

        //Create inventory and insert crates
        gui = plugin.getServer().createInventory(null, 27, "Crates");
        for (Crate crate : crates.values()) {
            addCrateToGui(crate);
        }
    }
    //#region Create
    public boolean createCrate(String name, ItemStack crate){

        Crate newCrate = new Crate(name, crate);
        crates.put(name, newCrate);
        addCrateToGui(newCrate);

        save();
        return true;
    }
    public void deleteCrate(String name){
        getCrateByName(name);
        config.set(name, null);
        save();
        crates.remove(name);

    }
    public void save() {
        for (String key : crates.keySet()) {
            config.set(key, crates.get(key));
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    HashMap<String, Crate> crates = new HashMap<>();

    public HashMap<String, Crate> getCrates() {
        return crates;
    }

    public void setCrates(HashMap<String, Crate> crates) {
        this.crates = crates;
    }
    public void addCrateToGui(Crate crate){
        ItemStack item = crate.getCrateItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(crate.getName());
        List<String> lore = new ArrayList<>();
        if (crate.getCrateItem().getItemMeta().hasLore()){
            for (String line : crate.getCrateItem().getItemMeta().getLore()) {
                lore.add(line);
            }
        }
        
        lore.add("");
        lore.add("Cost: " + crate.getOpenCost());
        lore.add("Key Required: " + crate.isKeyRequired());
        lore.add("Open From Inventory: " + crate.isOpenFromInventory());
        lore.add("Open By Block: " + crate.isOpenByBlock());
        lore.add("Block Linked: " + crate.getBlockLinked().getX() + ", " + crate.getBlockLinked().getY() + ", " + crate.getBlockLinked().getZ());
        lore.add("Text Display: " + crate.getTextDisplay().getText());
        lore.add("Rewards Count: " + crate.getRewards().length);
        lore.add("Broadcast Reward: " + crate.isBroadcastReward());

        meta.setLore(lore);
        gui.addItem(item);
    }
    //#region HandleOpening
    public void handleOpenByInventory(Player p, Crate crate){
        int random = (int) (Math.random() * crate.getRewards().length);
        ItemStack reward = crate.getRewards()[random];
        giveReward(p, reward, crate);
    }
    public void handleOpenByBlock(Player p, Crate crate){

        ItemStack key = crate.getKeyItem();
        Block b = crate.getBlockLinked();
        ItemDisplay itemDisplay = b.getWorld().spawn(b.getLocation().add(0.5, 0.5, 2), ItemDisplay.class);
        itemDisplay.setItemStack(key);
        itemDisplay.setCustomName("Opening..");
        itemDisplay.setCustomNameVisible(true);
        itemDisplay.setBillboard(Billboard.CENTER);

        new BukkitRunnable() {		
            int count = 10;
            
            @Override
            public void run() {	      
                p.playSound(p, Sound.UI_BUTTON_CLICK, 10, 10);
                int random = (int) (Math.random() * crate.getRewards().length);
                ItemStack reward = crate.getRewards()[random];
                giveReward(p, reward, crate);
                itemDisplay.setItemStack(reward);

                if (count == 0) {  
    		
                    cancel(); 
                    itemDisplay.remove();
                    return;
                }	    	    	                                    	    	                        

                count--;  
            }
        }.runTaskTimer(plugin, 0L, 10L); 

    }
    private void giveReward(Player p, ItemStack reward, Crate crate){
        if (reward.getType().isAir()){
            p.sendMessage("You got nothing, better luck next time.");
            return;
        }
        if (crate.isBroadcastReward()){
            plugin.getServer().broadcastMessage(p.getName() + " got " + reward.getItemMeta().getDisplayName() + " from " + crate.getDisplayName());
        }
        int space = p.getInventory().firstEmpty();
        if (space == -1){
            p.getWorld().dropItem(p.getLocation(), reward);
            p.sendMessage("Your inventory is full, item dropped on the ground.");
        } else {
            p.getInventory().addItem(reward);
            p.sendMessage("You got " + reward.getItemMeta().getDisplayName());
        }
    }
    public Crate getCrateByName(String name) {
        return crates.get(name);
    }
    public Crate getByBlock(Block block) {
        for (Crate crate : crates.values()) {
            if (crate.getBlockLinked().equals(block)) {
                return crate;
            }
        }
        return null;
    }
    // public Crate getByItemStack(ItemStack itemStack) {
    //     for (Crate crate : crates.values()) {
    //         if (crate.getCrateItem().equals(itemStack)) {
    //             return crate;
    //         }
    //     }
    //     return null;
    // }
    // public Crate getByKeyItem(ItemStack itemStack) {
    //     for (Crate crate : crates.values()) {
    //         if (crate.getKeyItem().equals(itemStack)) {
    //             return crate;
    //         }
    //     }
    //     return null;
    // }
    public ItemStack getCrateItem(Crate crate){
        ItemStack item = crate.getCrateItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, crate.getName());
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack getKeyItem(Crate crate){
        ItemStack item = crate.getKeyItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, crate.getName());
        item.setItemMeta(meta);
        return item;
    }
    public boolean isCreateKeyItem(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }
        return false;
    }
}
