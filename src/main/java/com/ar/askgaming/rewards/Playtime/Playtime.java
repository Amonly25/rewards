package com.ar.askgaming.rewards.Playtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.RewardsPlayerData;

public class Playtime extends BukkitRunnable {

    private RewardsPlugin plugin;

    public Playtime(RewardsPlugin plugin) {
        this.plugin = plugin;
        
        new Commands(plugin);
        runTaskTimer(plugin, 0, 20*60*60);
    }
    public String getPlaytimeFormmated(OfflinePlayer p) {
        int ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int totalMinutes = ticks / (20 * 60); // Convertir ticks a minutos
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int minutes = totalMinutes % 60;
        return days + "d " + hours + "h " + minutes + "m";
    }
    public int getPlaytimeMinutes(Player p) {
        int ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int totalMinutes = ticks / (20 * 60); // Convertir ticks a minutos
        return totalMinutes;
    }
    public void checkQueueRewards(Player p) {

        OfflinePlayer q = Bukkit.getOfflinePlayer(p.getUniqueId());
        if (queueRewards.containsKey(q)) {
            giveReward(p, queueRewards.get(q));
            queueRewards.remove(q);

        }
    }
    public void updatePlaytime(Player p) {
        RewardsPlayerData data = plugin.getDatabaseManager().loadPlayerData(p.getUniqueId());
        data.setPlaytime(p.getStatistic(Statistic.PLAY_ONE_MINUTE));
        data.save();
    }

    @Override
    public void run() {
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlaytime(p);
        }

        String day = plugin.getConfig().getString("playtime.check_on","SUNDAY");
        int hour = plugin.getConfig().getInt("playtime.at", 20);

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        if (date.getDayOfWeek().name().equals(day) && time.getHour() == hour){
            compareNow();           
                              
        }
    }
    public void compareNow(){
        plugin.getDatabaseManager().getPlaytimeTopAsync(top -> {
            checkTopForReward(top);
        });

    }
    private void checkTopForReward(HashMap<String,Integer> map) {
            
        // Crear una lista de las entradas del mapa
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());

        // Ordenar la lista usando un comparador por valor en orden descendente
        entryList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Suponiendo que `entryList` es la lista ordenada de los jugadores y su puntuación
        for (int i = 0; i < Math.min(entryList.size(), 3); i++) {
            UUID uuid = UUID.fromString(entryList.get(i).getKey());
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player.getPlayer() != null) {
                giveReward(player.getPlayer(), i);
            } else {
                queueRewards.put(player, i);
                plugin.getLogger().info("Player " + player.getName() + " is offline. Reward queued. If the server restarts, the reward will be lost.");
            }
        }
    }
    private void giveReward(Player player, int position) {
        String rewardPath = "playtime.rewards." + (position + 1); // Construir la ruta de configuración: rewards.1, rewards.2, rewards.3
        String message = plugin.getConfig().getString(rewardPath + ".message", "");
        String broadcast = plugin.getConfig().getString(rewardPath + ".broadcast", "");
        List<String> commands = plugin.getConfig().getStringList(rewardPath + ".commands");
        
        if (!message.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        if (!broadcast.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast.replace("%player%", player.getName())));
        }
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }
    private HashMap<OfflinePlayer, Integer> queueRewards = new HashMap<>();

    public void sendTop10(Player p){
        plugin.getDatabaseManager().getPlaytimeTopAsync(top -> {
            List<Map.Entry<String, Integer>> entryList = new ArrayList<>(top.entrySet());
            entryList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            p.sendMessage("§6Top 10 playtime:");
            for (int i = 0; i < Math.min(entryList.size(), 10); i++) {
                UUID uuid = UUID.fromString(entryList.get(i).getKey());
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                p.sendMessage("§e" + (i + 1) + ". " + player.getName() + " - " + getPlaytimeFormmated(player));
            }
        });
    }
}
