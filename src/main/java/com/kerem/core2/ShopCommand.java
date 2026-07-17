package com.kerem.core2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public ShopCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        String lang = plugin.playerLangs.getOrDefault(player.getUniqueId(), "EN");

        // ANA MENÜ: 54 Slotluk Dev Çanta
        String menuTitle = lang.equals("TR") ? "Market Kategorileri" : "Shop Categories";
        Inventory shopMenu = Bukkit.createInventory(null, 54, menuTitle);

        shopMenu.setItem(20, createCat(Material.DIAMOND_PICKAXE, lang.equals("TR") ? "Madenler" : "Ores"));
        shopMenu.setItem(21, createCat(Material.STONE, lang.equals("TR") ? "İnşaat Blokları" : "Building Blocks"));
        shopMenu.setItem(22, createCat(Material.BREAD, lang.equals("TR") ? "Yiyecekler" : "Food"));
        shopMenu.setItem(23, createCat(Material.ROTTEN_FLESH, lang.equals("TR") ? "Canavar Düşenleri" : "Mob Drops"));
        shopMenu.setItem(24, createCat(Material.DIAMOND_CHESTPLATE, lang.equals("TR") ? "Zırhlar" : "Armor"));
        shopMenu.setItem(31, createCat(Material.DIAMOND_SWORD, lang.equals("TR") ? "Aletler & Silahlar" : "Tools & Weapons"));

        player.openInventory(shopMenu);
        return true;
    }

    private ItemStack createCat(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + name);
        item.setItemMeta(meta);
        return item;
    }
}