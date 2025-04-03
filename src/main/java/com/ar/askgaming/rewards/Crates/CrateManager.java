package com.ar.askgaming.rewards.Crates;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.rewards.RewardsPlugin;

public class CrateManager {

    private RewardsPlugin plugin;
    private File file;
    private FileConfiguration config;
    private Inventory gui;
    private NamespacedKey key;

    private HashMap<String, Inventory> editing = new HashMap<>();
    private HashMap<String, Crate> crates = new HashMap<>();

    public CrateManager(RewardsPlugin plugin) {

        new CrateCommands(this);

        this.plugin = plugin;
        key = new NamespacedKey(plugin, "ask_crate");
        //Create File and load config
        file = new File(plugin.getDataFolder(), "crates.yml");

        //Create inventory and insert crates
        gui = plugin.getServer().createInventory(null, 27, "Crates");

        loadCrates();
    }
    public void loadCrates(){
        if (!file.exists()) {
            plugin.saveResource("crates.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = config.getKeys(false);
        if (keys.isEmpty()) return;

        //Load crates from config
        for (String key : keys) {
            try {
                Object obj = config.get(key);
                if (obj instanceof Crate) {
                    Crate crate = (Crate) obj;
                    crates.put(key, crate);
                } else {
                    plugin.getLogger().warning("Error loading crate '" + key + "': Invalid crate object, skipping...");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading crate '" + key + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    //#region Create
    public boolean createCrate(String name, ItemStack crate){

        Crate newCrate = new Crate(name, crate);
        crates.put(name, newCrate);
        addCrateToGui(newCrate);

        save(newCrate);
        return true;
    }
    //#region delete
    public void deleteCrate(String name){
        Crate crate = getCrateByName(name);
        if (crate.getTextDisplay() != null){
            crate.getTextDisplay().remove();
        }
        if (crate.getItemDisplay() != null){
            crate.getItemDisplay().remove();
        }
        config.set(name, null);
        crates.remove(name);
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateGui();

    }
    public void save(Crate crate) {
        try {
            config.set(crate.getName(), crate);
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().warning("Error saving crate: " + crate.getName());
        }
    }
    //#region AddCrateToGui
    public void addCrateToGui(Crate crate){
        ItemStack item = crate.getCrateItem().clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(crate.getName());
        item.setItemMeta(meta);
        
        List<String> lore = new ArrayList<>();
        if (crate.getCrateItem().getItemMeta().hasLore()){
            for (String line : crate.getCrateItem().getItemMeta().getLore()) {
                lore.add(line);
            }
        }
        
        lore.add("");
        lore.add("§7Cost: " + crate.getOpenCost());
        lore.add("§7Key Required: " + crate.isKeyRequired());
        lore.add("§7Open From Inventory: " + crate.isOpenFromInventory());
        lore.add("§7Open By Block: " + crate.isOpenByBlock());

        if (crate.getBlockLinked() == null){
            lore.add("§7Block Linked: None");

        } else {
            lore.add("§7Block Linked: " + crate.getBlockLinked().getX() + ", " + crate.getBlockLinked().getY() + ", " + crate.getBlockLinked().getZ());
        }
        if (crate.getTextDisplay() == null){
            lore.add("§7Text Display: None");
        } else {
            lore.add("§7Text Display: " + crate.getTextDisplay().getText());

        }

        lore.add("§7Rewards Count: " + crate.getRewards().length);
        lore.add("§7Broadcast Reward: " + crate.isBroadcastReward());

        meta.setLore(lore);
        item.setItemMeta(meta);
        gui.addItem(item);
    }
    //#region HandleOpening
    public void handleOpenByInventory(Player p, Crate crate){
        ItemStack[] contents = crate.getRewards();
        int random = (int) (Math.random() * contents.length);
        ItemStack reward = contents[random];
        giveReward(p, reward, crate);
    }
    public void handleOpenByBlock(Player p, Crate crate){

        ItemStack key = crate.getKeyItem();
        Block b = crate.getBlockLinked();
        ItemDisplay itemDisplay = b.getWorld().spawn(b.getLocation().add(0.5, 2, 0.5), ItemDisplay.class);
        itemDisplay.setItemStack(key);
        itemDisplay.setCustomName(plugin.getLangManager().getFrom("crates.opening", p));
        itemDisplay.setCustomNameVisible(true);
        itemDisplay.setItemDisplayTransform(ItemDisplayTransform.GROUND);
        itemDisplay.setBillboard(Billboard.CENTER);
        
        new BukkitRunnable() {		
            int count = 10;
            
            @Override
            public void run() {	      
                p.playSound(p, Sound.UI_BUTTON_CLICK, 10, 10);
                int random = (int) (Math.random() * crate.getRewards().length);
                ItemStack reward = crate.getRewards()[random];
                itemDisplay.setItemStack(reward);
                createParticleCircle(b.getLocation().add(0.5, 2, 0.5));

                if (count == 0) {  
                    giveReward(p, reward, crate);
                    cancel(); 
                    itemDisplay.remove();
                    return;
                }	    	    	                                    	    	                        

                count--;  
            }
        }.runTaskTimer(plugin, 0L, 10L); 

    }
    private void giveReward(Player p, ItemStack reward, Crate crate){
        if (reward == null || reward.getType() == Material.AIR){
            p.sendMessage(plugin.getLangManager().getFrom("crates.got_nothing", p));
            return;
        }
        String name = reward.getType().name().replace("_", " ");
        if (reward.getItemMeta().hasDisplayName()){
            name = reward.getItemMeta().getDisplayName();
        }
        if (crate.isBroadcastReward()){

            String crateName = crate.getCrateItem().getItemMeta().getDisplayName();

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(plugin.getLangManager().getFrom("crates.broadcast", p).replace("{player}", p.getName()).replace(
                    "{item}", name).replace("{crate}", crateName));
            }
        }
        int space = p.getInventory().firstEmpty();
        if (space == -1){
            p.getWorld().dropItem(p.getLocation(), reward);
            p.sendMessage(plugin.getLangManager().getFrom("misc.inventory_full", p));
        } else {
            p.getInventory().addItem(reward);
            p.sendMessage(plugin.getLangManager().getFrom("crates.got_item", p).replace("{item}", name));
        }
    }

    //#region getByBlock
    public Crate getByBlock(Block block) {
        for (Crate crate : crates.values()) {
            if (crate.getBlockLinked() != null) {
                if (crate.getBlockLinked().equals(block)) {
                    return crate;
                }
            }
        }
        return null;
    }
    public ItemStack getCrateItem(Crate crate){
        ItemStack item = crate.getCrateItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, crate.getName());
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack getKeyItem(Crate crate){
        ItemStack item = crate.getKeyItem();
        ItemMeta meta;
        if (item == null || item.getType() == Material.AIR) {
            item = crate.setDefaultKey();
            meta = item.getItemMeta();
        }
        meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, crate.getName());
        item.setItemMeta(meta);
        return item;
    }
    public boolean isCreateKeyItem(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }
        return false;
    }
    public boolean isCreateKeyItem(Crate crate, ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            
            if (crate.getName().equals(meta.getPersistentDataContainer().get(key, PersistentDataType.STRING))) {
                return true;
            }
        }
        return false;
    }
    //#region ItemRemove
    public boolean removeCrateKeyBeforetOpening(Player p, ItemStack item, ItemStack key, Crate crate){

        if (!hasRewards(crate)){
            p.sendMessage(plugin.getLangManager().getFrom("crates.no_rewards", p));
            return false;
        }

        if (item != null){
            if (item.getAmount() > 1){
                item.setAmount(item.getAmount() - 1);
            } else {
                item.setAmount(0);
            }
        }
        if (key!= null){
            if (key.getAmount() > 1){
                key.setAmount(key.getAmount() - 1);
            } else {
                key.setAmount(0);
            }
        }

        return true;
    }
    public void updateGui() {
        gui.clear();
        for (Crate crate : crates.values()) {
            addCrateToGui(crate);
        }
    }
    private boolean hasRewards(Crate crate) {
        return crate.getRewards().length > 0;
    }
    public void createParticleCircle(Location center) {

        List<Color> colors = List.of(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.PURPLE,
            Color.ORANGE,
            Color.AQUA,
            Color.FUCHSIA,
            Color.LIME,
            Color.MAROON
        );
        Color color = colors.get((int) (Math.random() * colors.size()));

        World world = center.getWorld();
        if (world == null) return;

        // Radio del círculo (1 bloque).
        double radius = 1.0;

        // Número de partículas a generar. Puedes ajustar este número para obtener un círculo más denso.
        int particleCount = 25;

        for (int i = 0; i < particleCount; i++) {
            // Ángulo aleatorio para distribuir las partículas en la esfera.
            double theta = Math.random() * 2 * Math.PI; // Ángulo en el plano XY
            double phi = Math.random() * Math.PI; // Ángulo desde el eje Z

            // Coordenadas esféricas.
            double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + radius * Math.sin(phi) * Math.sin(theta);
            double z = center.getZ() + radius * Math.cos(phi);

            Location particleLocation = new Location(world, x, y, z);
            world.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, 
                        new Particle.DustOptions(color, 1));
        }
    }
    //#region getters
    public Crate getCrateByName(String name) {
        return crates.get(name);
    }
    public NamespacedKey getKey() {
        return key;
    }
    public HashMap<String, Inventory> getEditing() {
        return editing;
    }
    public Inventory getGui() {
        return gui;
    }
    public HashMap<String, Crate> getCrates() {
        return crates;
    }


}
