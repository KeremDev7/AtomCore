package com.kerem.core2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public HomeCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        String lang = plugin.playerLangs.getOrDefault(player.getUniqueId(), "EN");

        // /sethome
        if (command.getName().equalsIgnoreCase("sethome")) {
            plugin.playerHomes.put(player.getUniqueId(), player.getLocation());
            player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "Evin başarıyla kaydedildi!" : ChatColor.GREEN + "Home successfully set!");
            return true;
        }

        // /home
        if (command.getName().equalsIgnoreCase("home")) {
            if (plugin.playerHomes.containsKey(player.getUniqueId())) {
                player.teleport(plugin.playerHomes.get(player.getUniqueId()));
                player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "Evine ışınlandın!" : ChatColor.GREEN + "Teleported to home!");
            } else {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Önce bir ev belirlemelisin! (/sethome)" : ChatColor.RED + "You need to set a home first! (/sethome)");
            }
            return true;
        }

        return true;
    }
}