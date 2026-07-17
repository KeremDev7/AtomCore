package com.kerem.core2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SBCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public SBCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        String lang = plugin.playerLangs.getOrDefault(player.getUniqueId(), "EN");

        if (plugin.hiddenScoreboards.contains(player.getUniqueId())) {
            plugin.hiddenScoreboards.remove(player.getUniqueId());
            player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "Tablo açıldı!" : ChatColor.GREEN + "Scoreboard enabled!");
        } else {
            plugin.hiddenScoreboards.add(player.getUniqueId());
            player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Tablo gizlendi!" : ChatColor.RED + "Scoreboard disabled!");
        }
        return true;
    }
}