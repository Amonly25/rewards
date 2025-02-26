package com.ar.askgaming.rewards.Referrals;

import java.io.File;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.RewardsPlayerData;

public class ReferralsManager extends BukkitRunnable{

    private RewardsPlugin plugin;

    public ReferralsManager(RewardsPlugin plugin) {
        this.plugin = plugin;

        new Commands(plugin, this);

        runTaskTimer(plugin, 20*60, 20*60);
    }

    private final Random random = new SecureRandom();

    public void createReferralCode(Player player) {

        String playerName = player.getName();

        String subString = playerName.substring(0, Math.min(3, playerName.length())).toUpperCase();

        int randomNumber = 1000 + random.nextInt(9000);

        if (existsCode(subString + randomNumber)) {
            createReferralCode(player);
            return;
        }

        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        data.setReferralCode(subString + randomNumber);
        data.save();

        
    }
    public String getRefferalCode(Player player) {
        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        String code = data.getReferralCode();
        if (code == null || code.isBlank()) return "Use /referral getcode";
        return code;
    }
    public String getReferralCode(String playerName) {

        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        FileConfiguration config = plugin.getConfig();
        if (player == null) return "Not found";
        String code = config.getString(player.getUniqueId()+ ".referral_code");
        if (code == null || code.isBlank())  return "Not found";
        return code;
        
    }

    private boolean existsCode(String code) {
        //plugin.getDataManager().getPlayerData().values().stream().filter(data -> data.getReferralCode().equals(code)).findFirst().orElse(null);
        return false;
    }
    public void checkOnJoin(Player p) {
        // is first join?
        int playtime = plugin.getPlaytimeManager().getPlaytimeMinutes(p);
        if (playtime >= 60) {
            return;
        }
        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(p.getUniqueId());
        if (data.getReferredBy() != null && !data.getReferredBy().isEmpty()) {
            return;
        }
        p.sendMessage("You has been referred by someone? Use /referral <code>, to claim your reward.");

    }

    public void onReferral(Player sender, String supposedCode){
        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(sender.getUniqueId());
        if (data.getReferredBy() != null && !data.getReferredBy().isEmpty()) {
            sender.sendMessage("You already claimed a referral.");
            return;
        }
        OfflinePlayer referredBy = plugin.getServer().getPlayer(supposedCode);
        if (referredBy == null) {
            sender.sendMessage("Code not found.");
            return;
        }
        processReferral(sender, supposedCode);

        Player player = referredBy.getPlayer();
        if (player != null) {
            RewardsPlayerData rData = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
            List<String> referredPlayers = rData.getReferredPlayers();
            referredPlayers.add(sender.getName());
            rData.setReferredPlayers(referredPlayers);
            rData.save();
            player.sendMessage("You have a new referred player: " + sender.getName());
            return;
        }
        // FileConfiguration off = plugin.getDataManager().getOfflinePlayerData(referredBy.getName());
        // List<String> referredPlayers = off.getStringList("referred_players");
        // referredPlayers.add(sender.getName());
        // off.set("referred_players", referredPlayers);
       // plugin.getDataManager().saveOfflinePlayerData(referredBy.getName(), off);
        
    }
    public OfflinePlayer getReferredBy(String code) {
        File folder = new File(plugin.getDataFolder(), "playerdata");
        File[] files = folder.listFiles();
        if (files == null) return null;
        for (File file : files) {
            String uuid = file.getName().replace(".yml", "");
            // FileConfiguration cfg = plugin.getDataManager().getOfflinePlayerData(uuid);
            // if (cfg.getString("referral_code").equals(code)) {
            //     return plugin.getServer().getOfflinePlayer(uuid);
            // }
        }
        return null;
    }

    @Override
    public void run() {
        
        // int max_minutes = plugin.getConfig().getInt("referral.after_how_much_playtime", 30);

        // for (Player p : plugin.getServer().getOnlinePlayers()) {
        //     RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(p.getUniqueId());
        //     if (data.getReferredBy() == null || data.getReferredBy().isEmpty()) continue;
        //     if (data.isGivedRewardToReferrer()) continue;

        //     int playtime = plugin.getPlaytimeManager().getPlaytimeMinutes(p);
        //     if (playtime < max_minutes) continue;

        //     giveRewardToReferrer(data.getReferredBy());
        //     data.setGivedRewardToReferrer(true);
        //     data.save();

        // }
    }
    public void processReferral(Player player, String code){
        plugin.getLogger().info("Processing reward to referred " + player.getName() + " with code " + code);
        giveRewardToReffered(player);
        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        data.setReferredBy(code);
        data.save();
    }
    public void giveRewardToReffered(Player p) {
        
       List<String> commands = plugin.getConfig().getStringList("referral.rewards.to_referred.commands");
       String message = plugin.getConfig().getString("referral.rewards.to_referred.message");

        for (String s : commands) {
            s = s.replace("%player%", p.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s);
        }
        p.sendMessage(message);
    }
    public void giveRewardToReferrer(String name) {
    List<String> commands = plugin.getConfig().getStringList("referral.rewards.to_referrer.commands");
       String message = plugin.getConfig().getString("referral.rewards.to_referrer.message");

        for (String s : commands) {
            s = s.replace("%player%", name);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s);
        }
        Player p = plugin.getServer().getPlayer(name);
        if (p != null) {
            p.sendMessage(message);
        }
    }

}
