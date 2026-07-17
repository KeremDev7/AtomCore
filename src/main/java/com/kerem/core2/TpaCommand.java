package com.kerem.core2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public TpaCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        String lang = plugin.playerLangs.getOrDefault(player.getUniqueId(), "EN");

        // /tpa <oyuncu>
        if (command.getName().equalsIgnoreCase("tpa")) {
            if (args.length == 0) {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Kullanım: /tpa <oyuncu>" : ChatColor.RED + "Usage: /tpa <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Oyuncu bulunamadı!" : ChatColor.RED + "Player not found!");
                return true;
            }
            if (target == player) {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Kendine ışınlanamazsın!" : ChatColor.RED + "You can't teleport to yourself!");
                return true;
            }

            plugin.tpaRequests.put(target.getUniqueId(), player.getUniqueId());
            player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "İstek gönderildi: " + target.getName() : ChatColor.GREEN + "Request sent to: " + target.getName());

            String targetLang = plugin.playerLangs.getOrDefault(target.getUniqueId(), "EN");
            target.sendMessage(targetLang.equals("TR") ? ChatColor.YELLOW + player.getName() + " sana ışınlanmak istiyor! Kabul etmek için /tpaccept, reddetmek için /tpdeny yaz."
                    : ChatColor.YELLOW + player.getName() + " wants to teleport to you! Type /tpaccept to accept, /tpdeny to deny.");
            return true;
        }

        // /tpaccept
        if (command.getName().equalsIgnoreCase("tpaccept")) {
            if (!plugin.tpaRequests.containsKey(player.getUniqueId())) {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Bekleyen bir isteğin yok!" : ChatColor.RED + "No pending requests!");
                return true;
            }
            Player senderPlayer = Bukkit.getPlayer(plugin.tpaRequests.get(player.getUniqueId()));
            if (senderPlayer != null && senderPlayer.isOnline()) {
                senderPlayer.teleport(player.getLocation());
                senderPlayer.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "İstek kabul edildi, ışınlandın!" : ChatColor.GREEN + "Request accepted, teleported!");
                player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + senderPlayer.getName() + " yanına ışınlandı." : ChatColor.GREEN + senderPlayer.getName() + " teleported to you.");
            }
            plugin.tpaRequests.remove(player.getUniqueId());
            return true;
        }

        // /tpdeny
        if (command.getName().equalsIgnoreCase("tpdeny")) {
            if (!plugin.tpaRequests.containsKey(player.getUniqueId())) {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Bekleyen bir isteğin yok!" : ChatColor.RED + "No pending requests!");
                return true;
            }
            Player senderPlayer = Bukkit.getPlayer(plugin.tpaRequests.get(player.getUniqueId()));
            if (senderPlayer != null && senderPlayer.isOnline()) {
                String senderLang = plugin.playerLangs.getOrDefault(senderPlayer.getUniqueId(), "EN");
                senderPlayer.sendMessage(senderLang.equals("TR") ? ChatColor.RED + player.getName() + " isteğini reddetti." : ChatColor.RED + player.getName() + " denied your request.");
            }
            player.sendMessage(lang.equals("TR") ? ChatColor.RED + "İstek reddedildi." : ChatColor.RED + "Request denied.");
            plugin.tpaRequests.remove(player.getUniqueId());
            return true;
        }

        return true;
    }
}