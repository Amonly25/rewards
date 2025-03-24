package com.ar.askgaming.rewards.Vote;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ar.askgaming.rewards.RewardsPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Commands implements CommandExecutor{

    private RewardsPlugin plugin;
    public Commands(RewardsPlugin plugin){
        this.plugin = plugin;

        plugin.getServer().getPluginCommand("vote").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            Player p = (Player) sender;
            p.sendMessage(plugin.getLangManager().getFrom("vote.vote", p));
            String from = plugin.getLangManager().getFrom("vote.link", p);
            List<String> list = plugin.getConfig().getStringList("vote_links");
            for (int i = 0; i < list.size(); i++){

                TextComponent clickableText = new TextComponent(from + (i + 1));
            
                clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, list.get(i)));

                clickableText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(list.get(i))));

                p.spigot().sendMessage(clickableText);
            }
            return true;
        }
        return false;
    }

}
