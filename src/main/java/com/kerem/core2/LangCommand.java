package com.kerem.core2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LangCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public LangCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        // Eğer oyuncu sadece /lang yazıp yanına bir şey eklemediyse uyar
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Kullanim / Usage: /lang turkce | /lang english");
            return true;
        }

        String choice = args[0].toLowerCase();

        // Türkçe seçimi
        if (choice.equals("turkce") || choice.equals("tr")) {
            plugin.playerLangs.put(player.getUniqueId(), "TR");
            player.sendMessage(ChatColor.GREEN + "Dil basariyla Turkce olarak ayarlandi!");
        }
        // İngilizce seçimi
        else if (choice.equals("english") || choice.equals("en")) {
            plugin.playerLangs.put(player.getUniqueId(), "EN");
            player.sendMessage(ChatColor.GREEN + "Language successfully set to English!");
        }
        // Yanlış bir şey yazarsa
        else {
            player.sendMessage(ChatColor.RED + "Gecersiz dil! / Invalid language! (turkce/english)");
            return true;
        }

        plugin.updateScoreboard(player);
        return true;
    }
}