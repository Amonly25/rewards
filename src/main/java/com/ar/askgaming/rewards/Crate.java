package com.ar.askgaming.rewards;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Crate implements ConfigurationSerializable {

    private RewardsPlugin plugin = RewardsPlugin.getPlugin(RewardsPlugin.class);

    private String name;
    private String displayName;
    private double openCost;
    private boolean isKeyRequired;
    private ItemStack crateItem;
    private boolean openFromInventory;
    private Block blockLinked;
    private boolean openByBlock;
    private ItemStack keyItem;
    private ItemStack[] rewards;
    private TextDisplay textDisplay;
    private ItemDisplay itemDisplay;
    private boolean broadcastReward;

    public Crate (String name, ItemStack item){
        if (item == null) {
            crateItem = new ItemStack(Material.CHEST);
        } else crateItem = item;

        this.name = name;
        this.displayName = name + " Crate";
        this.openCost = 0;
        this.isKeyRequired = false;
        this.openFromInventory = true;
        this.blockLinked = null;
        this.openByBlock = false;
        setDefaultKey();
        this.rewards = new ItemStack[0];
        this.textDisplay = null;
        this.itemDisplay = null;
        this.broadcastReward = true;

    }
    public Crate(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.displayName = (String) map.get("displayName");
        this.openCost = (double) map.get("openCost");
        this.isKeyRequired = (boolean) map.get("isKeyRequired");
        this.crateItem = (ItemStack) map.get("crateItem");
        this.openFromInventory = (boolean) map.get("openFromInventory");
        this.openByBlock = (boolean) map.get("openByBlock");

        if ( map.get("openByBlock") instanceof Location){
            Location loc = (Location) map.get("blockLinked");
            this.blockLinked = loc.getBlock();
            
        }
        this.keyItem = (ItemStack) map.get("keyItem");
        
        if (map.get("rewards") instanceof ItemStack[]) {
            this.rewards = (ItemStack[]) map.get("rewards");
        } else {
            this.rewards = new ItemStack[0];

        }
        this.broadcastReward = (boolean) map.get("broadcastReward");

        if (blockLinked != null) {
            createDefaultTextDisplay();
        }
    }
    //#region Serialize
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("displayName", displayName);
        map.put("openCost", openCost);
        map.put("isKeyRequired", isKeyRequired);
        map.put("crateItem", crateItem);
        map.put("keyItem", keyItem);
        map.put("rewards", rewards);
        map.put("openFromInventory", openFromInventory);

        if (blockLinked != null) {
            map.put("blockLinked", blockLinked.getLocation());
        } else {
            map.put("blockLinked", null);
        }
        map.put("openByBlock", openByBlock);
        map.put("broadcastReward", broadcastReward);
        return map;
    }
    public void setDefaultKey() {
        keyItem = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = keyItem.getItemMeta();
        meta.setDisplayName(name+" Key");
        keyItem.setItemMeta(meta);
    }
    public void createDefaultTextDisplay() {
        textDisplay = blockLinked.getWorld().spawn(blockLinked.getLocation().add(0.5, 1.5, 0.5), TextDisplay.class);
        textDisplay.setText(displayName);
        textDisplay.setBillboard(Billboard.CENTER);
    }
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getOpenCost() {
        return openCost;
    }

    public boolean isKeyRequired() {
        return isKeyRequired;
    }

    public ItemStack getCrateItem() {       
        return crateItem;
    }

    public boolean isOpenFromInventory() {
        return openFromInventory;
    }

    public Block getBlockLinked() {
        return blockLinked;
    }

    public boolean isOpenByBlock() {
        return openByBlock;
    }

    public ItemStack getKeyItem() {
        return keyItem;
    }

    public ItemStack[] getRewards() {
        return rewards;
    }

    public TextDisplay getTextDisplay() {
        return textDisplay;
    }

    public ItemDisplay getItemDisplay() {
        return itemDisplay;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public void setOpenCost(double openCost) {
        this.openCost = openCost;
    }

    public void setKeyRequired(boolean isKeyRequired) {
        this.isKeyRequired = isKeyRequired;
    }
    public void setCrateItem(ItemStack crateItem) {
        this.crateItem = crateItem;
    }
    public void setOpenFromInventory(boolean openFromInventory) {
        this.openFromInventory = openFromInventory;
    }
    public void setBlockLinked(Block blockLinked) {
        this.blockLinked = blockLinked;
        createDefaultTextDisplay();
        
    }
    public void setOpenByBlock(boolean openByBlock) {
        this.openByBlock = openByBlock;
    }
    public void setKeyItem(ItemStack keyItem) {
        this.keyItem = keyItem;
    }
    public void setRewards(ItemStack[] rewards) {
        this.rewards = rewards;
    }
    public void setTextDisplay(TextDisplay textDisplay) {
        this.textDisplay = textDisplay;
    }
    public void setItemDisplay(ItemDisplay itemDisplay) {
        this.itemDisplay = itemDisplay;
    }
    public boolean isBroadcastReward() {
        return broadcastReward;
    }
    public void setBroadcastReward(boolean broadcastReward) {
        this.broadcastReward = broadcastReward;
    }

}
