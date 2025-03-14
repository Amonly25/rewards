package com.ar.askgaming.rewards.Utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.RewardsPlayerData;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolders extends PlaceholderExpansion {

    private final RewardsPlugin plugin;

    public PlaceHolders(RewardsPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public String onRequest(OfflinePlayer player, String params) {

        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        if (data == null) return "No player data";

        switch (params) {
            case "playtime":
                return plugin.getPlaytimeManager().getPlaytimeFormmated(player);
            case "votes":
                return String.valueOf(data.getVotes());
            case "streak_connection":
                return String.valueOf(data.getStreakConnection());
            case "next_daily":
                Player p = Bukkit.getPlayer(player.getUniqueId());
                if (p == null) return "Player not online";
                return plugin.getDailyReward().getText(p);
            case "referrals":
                return String.valueOf(data.getReferredPlayers().size());
            case "referral_code":
                return data.getReferralCode();
            case "referred_by":
                return data.getReferredBy();
            default:
                return "Invalid Placeholder";
        }
    }

    @Override
    public String getAuthor() {
        return "Askgaming";
    }

    @Override
    public String getIdentifier() {
        return "rewards";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
