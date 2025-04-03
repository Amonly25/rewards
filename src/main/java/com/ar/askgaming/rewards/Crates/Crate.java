package com.ar.askgaming.rewards.Crates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Crate implements ConfigurationSerializable {

    private String name, displayText;
    private double openCost;
    private Boolean isKeyRequired, openByBlock, broadcastReward, openFromInventory;
    private ItemStack crateItem, keyItem;
    private Block blockLinked;
    private ItemStack[] rewards;
    private TextDisplay textDisplay;
    private ItemDisplay itemDisplay;

    public Crate (String name, ItemStack item){
        if (item == null) {
            crateItem = new ItemStack(Material.CHEST);
            ItemMeta meta = crateItem.getItemMeta();
            meta.setDisplayName("§6" + name+" Crate");

            crateItem.setItemMeta(meta);

        } else crateItem = item;

        this.name = name;
        this.openCost = 0;
        this.isKeyRequired = false;
        this.openFromInventory = true;
        this.blockLinked = null;
        this.openByBlock = false;
        this.rewards = new ItemStack[0];
        this.textDisplay = null;
        this.itemDisplay = null;
        this.broadcastReward = true;
        this.displayText = "§6" + name + " Crate";

        setDefaultKey();

    }
    public Crate(Map<String, Object> map) {

        this.name = (String) map.get("name");
        this.openCost = ((Number) map.get("openCost")).doubleValue();
        this.isKeyRequired = (boolean) map.get("isKeyRequired");
        this.crateItem = (ItemStack) map.get("crateItem");
        this.openFromInventory = (boolean) map.get("openFromInventory");
        this.openByBlock = (boolean) map.get("openByBlock");
        this.displayText = (String) map.get("displayText");

        Object blockObj = map.get("blockLinked");
        if (blockObj instanceof Location loc) {
            this.blockLinked = loc.getBlock();
        }
        if (this.blockLinked != null) {
            createDefaultTextDisplay();
        }

        this.keyItem = (ItemStack) map.get("keyItem");

        Object object = map.get("rewards");
        if (object instanceof ItemStack[]) {
            this.rewards = (ItemStack[]) map.get("rewards");
        } else if (object instanceof List) {
            @SuppressWarnings("unchecked")
            List<ItemStack> list = (List<ItemStack>) object;
            this.rewards = list.toArray(new ItemStack[0]);

        } else rewards = new ItemStack[0];
        
        this.broadcastReward = (boolean) map.get("broadcastReward");

    }
    //#region Serialize
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("openCost", openCost);
        map.put("isKeyRequired", isKeyRequired);
        map.put("crateItem", crateItem);
        map.put("keyItem", keyItem);
        map.put("rewards", rewards);
        map.put("openFromInventory", openFromInventory);
        map.put("displayText", displayText);
        map.put("openByBlock", openByBlock);
        map.put("broadcastReward", broadcastReward);

        if (blockLinked != null) {
            map.put("blockLinked", blockLinked.getLocation());
        } else {
            map.put("blockLinked", null);
        }

        return map;
    }
    //#region defaultKey
    public ItemStack setDefaultKey() {
        keyItem = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = keyItem.getItemMeta();
        meta.setDisplayName("§6" + name+" Key");

        keyItem.setItemMeta(meta);
        return keyItem;
    }
    //#region defaultDis
    public void createDefaultTextDisplay() {
        if (blockLinked == null || blockLinked.getWorld() == null) {
            return;
        }
        if (textDisplay != null) {
            textDisplay.remove();
        }
        
        textDisplay = blockLinked.getWorld().spawn(blockLinked.getLocation().add(0.5, 1, 0.5), TextDisplay.class);

        String name;
        if (displayText != null) {
            name = displayText;
        } else {
            name = "§6" + this.name + " Crate";
        }
        textDisplay.setText(ChatColor.translateAlternateColorCodes('&', name));
        textDisplay.setBillboard(Billboard.CENTER);
        textDisplay.setLineWidth(128);
    }
    public String getName() {
        return name;
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

    public void setOpenCost(double openCost) {
        this.openCost = openCost;
    }

    public void setKeyRequired(boolean isKeyRequired) {
        this.isKeyRequired = isKeyRequired;
    }
    public void setCrateItem(ItemStack crateItem) {
        this.crateItem = crateItem.clone();
    }
    public void setOpenFromInventory(boolean openFromInventory) {
        this.openFromInventory = openFromInventory;
    }
    public void setBlockLinked(Block blockLinked) {
        this.blockLinked = blockLinked;
        if (blockLinked != null) {
            createDefaultTextDisplay();
        }
        
    }
    public void setOpenByBlock(boolean openByBlock) {
        this.openByBlock = openByBlock;
    }
    public void setKeyItem(ItemStack keyItem) {
        this.keyItem = keyItem.clone();
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
    public String getDisplayText() {
        return displayText;
    }
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

}
