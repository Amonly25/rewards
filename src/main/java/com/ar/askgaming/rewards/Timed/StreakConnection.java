package com.ar.askgaming.rewards.Timed;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;
import com.ar.askgaming.rewards.Managers.RewardsPlayerData;

public class StreakConnection {

    private RewardsPlugin plugin;
    public StreakConnection(RewardsPlugin plugin){
        this.plugin = plugin;
    }
    public void process(Player p){
        RewardsPlayerData pData = plugin.getDatabaseManager().loadPlayerData(p.getUniqueId());

		if (lastConnection(p).isEmpty()){
            pData.setLastConnection(getToday());
			pData.save();
            return;
        }

		if (!lastConnection(p).equals(getToday())) {
									
			if (hasConnectedYesterday(p)) {	

                int streak = pData.getStreakConnection()+1;
				p.sendMessage(plugin.getLangManager().getFrom("streak.on_join", p).replace("{streak}", streak+""));

                pData.setStreakConnection(streak);

                for (String key : plugin.getConfig().getConfigurationSection("streak_connection.rewards").getKeys(false)) {
				 
                    if (Integer.valueOf(key) <= streak) {
                            
                        String message = plugin.getConfig().getString("streak_connection.rewards." + key + ".message","");
                        List<String> commands = plugin.getConfig().getStringList("streak_connection.rewards." + key + ".commands");
						String broadcast = plugin.getConfig().getString("streak_connection.rewards." + key  + ".broadcast", "");

						if (!broadcast.equals("")) {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast).replace("%player%", p.getName()));
						}
                        
                        if (!message.equals("")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message).replace("%streak%", streak+""));
                        }
                        
                        for (String s : commands){
                            s = s.replace("%player%", p.getName()).replace("%streak%", streak+"");
                            //Bukkit.broadcastMessage(s);
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s);
                        }				
                    }			
                }
			}
			else if (resetStrekConnections(p)) {
                pData.setStreakConnection(0);	
			}
            pData.setLastConnection(getToday());
			pData.save();
		}	
    }

    private DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy");
	
	public String getToday() {	
		
		Calendar cal = Calendar.getInstance();		
		String format = dateFormat.format(cal.getTime());
		
		return format;		
	}
	private Date yesterday() {
		
	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	public String getYesterday() {
        return dateFormat.format(yesterday());
	}
	
	public String lastConnection(Player player) {	
		RewardsPlayerData files = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
		return files.getLastConnection();
	}
	
	public boolean resetStrekConnections(Player player){
		
		if (lastConnection(player).equals(getYesterday())) {
			return false;
		}
		return true;
	}
	
	public boolean hasConnectedYesterday(Player player){
		
		if (lastConnection(player).equals(getYesterday())) {
			return true;
		}
		return false;
	}
    

}
