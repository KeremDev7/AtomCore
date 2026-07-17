package com.kerem.core2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public HelpCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        // Get player language / Oyuncu dilini cek
        String lang = plugin.playerLangs.getOrDefault(player.getUniqueId(), "EN");

        if (lang.equals("TR")) {
            player.sendMessage(ChatColor.GOLD + "=== AtomCore Yardim Menusu ===");
            player.sendMessage(ChatColor.YELLOW + "/help" + ChatColor.WHITE + " - Bu menuyu acar.");
            player.sendMessage(ChatColor.YELLOW + "/lang <turkce/english>" + ChatColor.WHITE + " - Oyun dilini degistirir.");
            player.sendMessage(ChatColor.YELLOW + "/shop" + ChatColor.WHITE + " - Market menusunu acar.");
            player.sendMessage(ChatColor.YELLOW + "/tpa <oyuncu>" + ChatColor.WHITE + " - Isinlanma istegi gonderir.");
            player.sendMessage(ChatColor.YELLOW + "/tpaccept | /tpdeny" + ChatColor.WHITE + " - Istegi kabul eder / reddeder.");
            player.sendMessage(ChatColor.YELLOW + "/sethome | /home" + ChatColor.WHITE + " - Evini kaydeder / evine isinlar.");
            player.sendMessage(ChatColor.YELLOW + "/sb" + ChatColor.WHITE + " - Yandaki tabloyu gizler / acar.");
            player.sendMessage(ChatColor.GOLD + "==============================");
        } else {
            player.sendMessage(ChatColor.GOLD + "=== AtomCore Help Menu ===");
            player.sendMessage(ChatColor.YELLOW + "/help" + ChatColor.WHITE + " - Displays this menu.");
            player.sendMessage(ChatColor.YELLOW + "/lang <turkce/english>" + ChatColor.WHITE + " - Changes the game language.");
            player.sendMessage(ChatColor.YELLOW + "/shop" + ChatColor.WHITE + " - Opens the server shop GUI.");
            player.sendMessage(ChatColor.YELLOW + "/tpa <player>" + ChatColor.WHITE + " - Sends a teleport request.");
            player.sendMessage(ChatColor.YELLOW + "/tpaccept | /tpdeny" + ChatColor.WHITE + " - Accepts / denies a request.");
            player.sendMessage(ChatColor.YELLOW + "/sethome | /home" + ChatColor.WHITE + " - Sets / teleports to your home.");
            player.sendMessage(ChatColor.YELLOW + "/sb" + ChatColor.WHITE + " - Toggles the sidebar scoreboard.");
            player.sendMessage(ChatColor.GOLD + "==========================");
        }
        return true;
    }
}