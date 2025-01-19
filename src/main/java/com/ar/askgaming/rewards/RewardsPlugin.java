package com.ar.askgaming.rewards;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.rewards.Commands.CrateCommands;
import com.ar.askgaming.rewards.Commands.RewardsCommands;
import com.ar.askgaming.rewards.Crates.Crate;
import com.ar.askgaming.rewards.Crates.CrateManager;
import com.ar.askgaming.rewards.Listeners.BlockBreakListener;
import com.ar.askgaming.rewards.Listeners.CloseInventoryListener;
import com.ar.askgaming.rewards.Listeners.CreatureSpawnListener;
import com.ar.askgaming.rewards.Listeners.EntityDismountListener;
import com.ar.askgaming.rewards.Listeners.InventoryClickListener;
import com.ar.askgaming.rewards.Listeners.OpenInventoryListener;
import com.ar.askgaming.rewards.Listeners.PickUpItemListener;
import com.ar.askgaming.rewards.Listeners.PlaceBlockListener;
import com.ar.askgaming.rewards.Listeners.PlayerInteractListener;
import com.ar.askgaming.rewards.Listeners.PlayerJoinListener;
import com.ar.askgaming.rewards.Managers.DataManager;
import com.ar.askgaming.rewards.Managers.LangManager;
import com.ar.askgaming.rewards.Managers.PlayerData;
import com.ar.askgaming.rewards.Rewards.DailyReward;
import com.ar.askgaming.rewards.Rewards.RewardsGui;
import com.ar.askgaming.rewards.Rewards.StreakConnection;

public class RewardsPlugin extends JavaPlugin {
    
    private CrateManager crateManager;
    private LangManager langManager;
    private DataManager dataManager;
    private DailyReward dailyReward;
    private StreakConnection streakConnection;
    private RewardsGui rewardsGui;

    public void onEnable() {

        saveDefaultConfig();
        
        ConfigurationSerialization.registerClass(Crate.class,"Crate");
        ConfigurationSerialization.registerClass(PlayerData.class,"PlayerData");

        crateManager = new CrateManager(this);
        langManager = new LangManager(this);
        rewardsGui = new RewardsGui(this);
        dataManager = new DataManager(this);
        dailyReward = new DailyReward(this);
        streakConnection = new StreakConnection(this);

        getServer().getPluginCommand("rewards").setExecutor(new RewardsCommands(this));
        getServer().getPluginCommand("crate").setExecutor(new CrateCommands(this));

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlaceBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new OpenInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CloseInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PickUpItemListener(this), this);

        new EntityDismountListener(this);
    }

    public void onDisable() {
        crateManager.getGui().getViewers().forEach(viewer -> viewer.closeInventory());

        crateManager.getCrates().forEach((name, crate) -> {
            if (crate.getTextDisplay()!=null) {
                crate.getTextDisplay().remove();

            }
            if (crate.getItemDisplay()!=null) {
                crate.getItemDisplay().remove();
            }
        });
    }
    public CrateManager getCrateManager() {
        return crateManager;
    }
    public LangManager getLangManager() {
        return langManager;
    }
    public RewardsGui getRewardsGui() {
        return rewardsGui;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
    public StreakConnection getStreakConnection() {
        return streakConnection;
    }

    public DailyReward getDailyReward() {
        return dailyReward;
    }

}