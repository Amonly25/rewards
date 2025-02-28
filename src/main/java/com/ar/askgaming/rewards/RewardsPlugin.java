package com.ar.askgaming.rewards;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.rewards.Crates.Crate;
import com.ar.askgaming.rewards.Crates.CrateManager;
import com.ar.askgaming.rewards.Listeners.*;
import com.ar.askgaming.rewards.Listeners.PlayerListeners.*;
import com.ar.askgaming.rewards.Managers.DataManager;
import com.ar.askgaming.rewards.Managers.DatabaseManager;
import com.ar.askgaming.rewards.Managers.LangManager;
import com.ar.askgaming.rewards.Managers.PlayerData;
import com.ar.askgaming.rewards.Playtime.Playtime;
import com.ar.askgaming.rewards.Referrals.ReferralsManager;
import com.ar.askgaming.rewards.Timed.Daily;
import com.ar.askgaming.rewards.Timed.StreakConnection;
import com.ar.askgaming.rewards.Vote.VoteListener;

import fr.xephi.authme.api.v3.AuthMeApi;

public class RewardsPlugin extends JavaPlugin {
    
    private CrateManager crateManager;
    private LangManager langManager;
    private DataManager dataManager;
    private Daily dailyReward;
    private StreakConnection streakConnection;
    private RewardsGui rewardsGui;
    private ReferralsManager referrals;
    private VoteListener vote;
    private Playtime playtime;
    private DatabaseManager databaseManager;

    private AuthMeApi authMeApi;

    public void onEnable() {

        saveDefaultConfig();
        
        ConfigurationSerialization.registerClass(Crate.class,"Crate");
        ConfigurationSerialization.registerClass(PlayerData.class,"PlayerData");

        databaseManager = new DatabaseManager(this);

        try (Connection conn = databaseManager.connect()) {
            getLogger().info("Connected to database.");
            databaseManager.createTable();
            databaseManager.createRefferalsCodeTable();
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to the database. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            e.printStackTrace();
        }

        crateManager = new CrateManager(this);
        langManager = new LangManager(this);
        rewardsGui = new RewardsGui(this);
        dataManager = new DataManager(this);
        dailyReward = new Daily(this);
        streakConnection = new StreakConnection(this);
        referrals = new ReferralsManager(this);
        playtime = new Playtime(this);

        new RewardsCommands(this);

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlaceBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new OpenInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CloseInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PickUpItemListener(this), this);
        
        new CreatureSpawnListener(this);
        new EntityDismountListener(this);
        new EntityPortalListener(this);

        if (getServer().getPluginManager().getPlugin("VotifierPlus") != null) {
            vote = new VoteListener(this);
        }

        if (getServer().getPluginManager().getPlugin("AuthMe") != null) {
            authMeApi = AuthMeApi.getInstance();
            new PlayerLoginListener(this);
        }
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
    public AuthMeApi getAuthMeApi() {
        return authMeApi;
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
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
    public ReferralsManager getReferrals() {
        return referrals;
    }

    public Daily getDailyReward() {
        return dailyReward;
    }
    public VoteListener getVoteReward() {
        return vote;
    }
    public Playtime getPlaytimeManager() {
        return playtime;
    }
    public void setRewardsGui(RewardsGui rewardsGui) {
        this.rewardsGui = rewardsGui;
    }

}