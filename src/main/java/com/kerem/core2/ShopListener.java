package com.kerem.core2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShopListener implements Listener {

    private final CorePlugin plugin;
    private final HashMap<UUID, Material> pendingSells = new HashMap<>();

    public ShopListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.equals("Emin misin?") || title.equals("Are you sure?")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();
            String lang = plugin.playerLangs.getOrDefault(player.getUniqueId(), "EN");
            Material mat = pendingSells.get(player.getUniqueId());

            if (event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
                if (mat != null) executeSellAll(player, mat, lang);
                player.closeInventory();
            } else if (event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
                player.closeInventory();
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Satış iptal edildi." : ChatColor.RED + "Sale cancelled.");
            }
            return;
        }

        if (title.equals("Market Kategorileri") || title.equals("Shop Categories") ||
                title.equals("Madenler") || title.equals("Ores") ||
                title.equals("İnşaat Blokları") || title.equals("Building Blocks") ||
                title.equals("Yiyecekler") || title.equals("Food") ||
                title.equals("Canavar Düşenleri") || title.equals("Mob Drops") ||
                title.equals("Zırhlar") || title.equals("Armor") ||
                title.equals("Aletler & Silahlar") || title.equals("Tools & Weapons")) {

            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            String lang = plugin.playerLangs.getOrDefault(player.getUniqueId(), "EN");
            Material clickedType = event.getCurrentItem().getType();
            String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

            if (clickedType == Material.ARROW && itemName.contains("⬅")) {
                player.performCommand("shop");
                return;
            }

            if (title.equals("Market Kategorileri") || title.equals("Shop Categories")) {
                if (clickedType == Material.DIAMOND_PICKAXE) openOresMenu(player, lang);
                else if (clickedType == Material.STONE) openBlocksMenu(player, lang);
                else if (clickedType == Material.BREAD) openFoodMenu(player, lang);
                else if (clickedType == Material.ROTTEN_FLESH) openDropsMenu(player, lang);
                else if (clickedType == Material.DIAMOND_CHESTPLATE) openArmorMenu(player, lang);
                else if (clickedType == Material.DIAMOND_SWORD) openToolsMenu(player, lang);
                return;
            }

            double buyPrice = getBuyPrice(clickedType);
            double sellPrice = getSellPrice(clickedType);
            if (buyPrice == 0) return;

            if (event.isLeftClick()) {
                buyItem(player, clickedType, buyPrice, lang);
            } else if (event.isRightClick()) {
                if (event.isShiftClick()) {
                    pendingSells.put(player.getUniqueId(), clickedType);
                    openConfirmMenu(player, lang);
                } else {
                    sellItem(player, clickedType, sellPrice, lang);
                }
            }
        }
    }

    private void openConfirmMenu(Player player, String lang) {
        String title = lang.equals("TR") ? "Emin misin?" : "Are you sure?";
        Inventory inv = Bukkit.createInventory(null, 9, title);

        ItemStack yes = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(lang.equals("TR") ? ChatColor.GREEN + "EVET, Hepsini Sat" : ChatColor.GREEN + "YES, Sell All");
        yes.setItemMeta(yesMeta);

        ItemStack no = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(lang.equals("TR") ? ChatColor.RED + "HAYIR, İptal Et" : ChatColor.RED + "NO, Cancel");
        no.setItemMeta(noMeta);

        inv.setItem(3, yes);
        inv.setItem(5, no);
        player.openInventory(inv);
    }

    private void executeSellAll(Player player, Material mat, String lang) {
        double pricePerItem = getSellPrice(mat);
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) count += item.getAmount();
        }

        if (count > 0) {
            double totalEarned = count * pricePerItem;
            double currentMoney = plugin.playerMoney.get(player.getUniqueId());

            // MAKSİMUM PARA KONTROLÜ
            if (currentMoney + totalEarned > plugin.maxMoney) {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Bu satış seni maksimum para sınırına ulaştırıyor! (" + plugin.currencySymbol + plugin.maxMoney + ")" : ChatColor.RED + "This sale exceeds the maximum money limit! (" + plugin.currencySymbol + plugin.maxMoney + ")");
                return;
            }

            player.getInventory().removeItem(new ItemStack(mat, count));
            plugin.playerMoney.put(player.getUniqueId(), currentMoney + totalEarned);
            player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "" + count + " adet sattın! (+" + plugin.currencySymbol + totalEarned + ")" : ChatColor.GREEN + "Sold " + count + " items! (+" + plugin.currencySymbol + totalEarned + ")");
            plugin.updateScoreboard(player);
        } else {
            player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Envanterinde bu eşyadan yok!" : ChatColor.RED + "You don't have this item!");
        }
    }

    private void buyItem(Player player, Material mat, double price, String lang) {
        double currentMoney = plugin.playerMoney.get(player.getUniqueId());
        if (currentMoney >= price) {
            plugin.playerMoney.put(player.getUniqueId(), currentMoney - price);
            player.getInventory().addItem(new ItemStack(mat, 1));
            player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "Satın aldın! (-" + plugin.currencySymbol + price + ")" : ChatColor.GREEN + "You bought it! (-" + plugin.currencySymbol + price + ")");
            plugin.updateScoreboard(player);
        } else {
            player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Yeterli paran yok!" : ChatColor.RED + "Not enough money!");
        }
    }

    private void sellItem(Player player, Material mat, double price, String lang) {
        if (player.getInventory().containsAtLeast(new ItemStack(mat), 1)) {
            double currentMoney = plugin.playerMoney.get(player.getUniqueId());

            // MAKSİMUM PARA KONTROLÜ
            if (currentMoney + price > plugin.maxMoney) {
                player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Maksimum para sınırına ulaştın! (" + plugin.currencySymbol + plugin.maxMoney + ")" : ChatColor.RED + "You reached the max money limit! (" + plugin.currencySymbol + plugin.maxMoney + ")");
                return;
            }

            player.getInventory().removeItem(new ItemStack(mat, 1));
            plugin.playerMoney.put(player.getUniqueId(), currentMoney + price);
            player.sendMessage(lang.equals("TR") ? ChatColor.GREEN + "Sattın! (+" + plugin.currencySymbol + price + ")" : ChatColor.GREEN + "Sold! (+" + plugin.currencySymbol + price + ")");
            plugin.updateScoreboard(player);
        } else {
            player.sendMessage(lang.equals("TR") ? ChatColor.RED + "Envanterinde bu eşyadan yok!" : ChatColor.RED + "You don't have this item!");
        }
    }

    private double getBuyPrice(Material mat) {
        switch (mat) {
            case DIAMOND: return 100.0; case IRON_INGOT: return 50.0; case GOLD_INGOT: return 75.0; case EMERALD: return 120.0; case COAL: return 10.0; case NETHERITE_INGOT: return 1500.0; case LAPIS_LAZULI: return 15.0; case REDSTONE: return 10.0; case COPPER_INGOT: return 20.0;
            case STONE: return 10.0; case COBBLESTONE: return 5.0; case DIRT: return 5.0; case OAK_LOG: return 20.0; case BIRCH_LOG: return 20.0; case SPRUCE_LOG: return 20.0; case GLASS: return 15.0; case SAND: return 8.0; case GRAVEL: return 8.0; case OBSIDIAN: return 200.0;
            case BREAD: return 10.0; case COOKED_BEEF: return 25.0; case GOLDEN_APPLE: return 250.0; case CARROT: return 5.0; case POTATO: return 5.0; case APPLE: return 10.0; case MELON_SLICE: return 5.0; case COOKED_CHICKEN: return 15.0;
            case ROTTEN_FLESH: return 5.0; case BONE: return 10.0; case GUNPOWDER: return 25.0; case ENDER_PEARL: return 75.0; case SPIDER_EYE: return 15.0; case STRING: return 10.0; case SLIME_BALL: return 40.0; case BLAZE_ROD: return 100.0;
            case DIAMOND_HELMET: return 500.0; case DIAMOND_CHESTPLATE: return 800.0; case DIAMOND_LEGGINGS: return 700.0; case DIAMOND_BOOTS: return 400.0;
            case IRON_HELMET: return 250.0; case IRON_CHESTPLATE: return 400.0; case IRON_LEGGINGS: return 350.0; case IRON_BOOTS: return 200.0;
            case DIAMOND_SWORD: return 400.0; case DIAMOND_PICKAXE: return 600.0; case DIAMOND_AXE: return 600.0; case DIAMOND_SHOVEL: return 200.0;
            case IRON_SWORD: return 200.0; case IRON_PICKAXE: return 300.0; case IRON_AXE: return 300.0; case IRON_SHOVEL: return 100.0; case BOW: return 150.0; case ARROW: return 5.0;
            default: return 0.0;
        }
    }

    private double getSellPrice(Material mat) {
        switch (mat) {
            case DIAMOND: return 50.0; case IRON_INGOT: return 25.0; case GOLD_INGOT: return 35.0; case EMERALD: return 60.0; case COAL: return 5.0; case NETHERITE_INGOT: return 750.0; case LAPIS_LAZULI: return 7.0; case REDSTONE: return 5.0; case COPPER_INGOT: return 10.0;
            case STONE: return 5.0; case COBBLESTONE: return 2.0; case DIRT: return 1.0; case OAK_LOG: return 10.0; case BIRCH_LOG: return 10.0; case SPRUCE_LOG: return 10.0; case GLASS: return 5.0; case SAND: return 3.0; case GRAVEL: return 3.0; case OBSIDIAN: return 100.0;
            case BREAD: return 5.0; case COOKED_BEEF: return 12.0; case GOLDEN_APPLE: return 100.0; case CARROT: return 2.0; case POTATO: return 2.0; case APPLE: return 5.0; case MELON_SLICE: return 2.0; case COOKED_CHICKEN: return 7.0;
            case ROTTEN_FLESH: return 2.0; case BONE: return 5.0; case GUNPOWDER: return 10.0; case ENDER_PEARL: return 30.0; case SPIDER_EYE: return 5.0; case STRING: return 5.0; case SLIME_BALL: return 20.0; case BLAZE_ROD: return 50.0;
            case DIAMOND_HELMET: return 250.0; case DIAMOND_CHESTPLATE: return 400.0; case DIAMOND_LEGGINGS: return 350.0; case DIAMOND_BOOTS: return 200.0;
            case IRON_HELMET: return 125.0; case IRON_CHESTPLATE: return 200.0; case IRON_LEGGINGS: return 175.0; case IRON_BOOTS: return 100.0;
            case DIAMOND_SWORD: return 200.0; case DIAMOND_PICKAXE: return 300.0; case DIAMOND_AXE: return 300.0; case DIAMOND_SHOVEL: return 100.0;
            case IRON_SWORD: return 100.0; case IRON_PICKAXE: return 150.0; case IRON_AXE: return 150.0; case IRON_SHOVEL: return 50.0; case BOW: return 75.0; case ARROW: return 2.0;
            default: return 0.0;
        }
    }

    private void setBackButton(Inventory inv, String lang) {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(lang.equals("TR") ? ChatColor.RED + "⬅ Geri Dön" : ChatColor.RED + "⬅ Go Back");
        back.setItemMeta(backMeta);
        inv.setItem(31, back);
    }

    private void openOresMenu(Player player, String lang) {
        Inventory inv = Bukkit.createInventory(null, 36, lang.equals("TR") ? "Madenler" : "Ores");
        inv.setItem(10, createDisplayItem(Material.COAL, lang.equals("TR") ? "Kömür" : "Coal", 10, 5, lang));
        inv.setItem(11, createDisplayItem(Material.IRON_INGOT, lang.equals("TR") ? "Demir" : "Iron", 50, 25, lang));
        inv.setItem(12, createDisplayItem(Material.GOLD_INGOT, lang.equals("TR") ? "Altın" : "Gold", 75, 35, lang));
        inv.setItem(13, createDisplayItem(Material.DIAMOND, lang.equals("TR") ? "Elmas" : "Diamond", 100, 50, lang));
        inv.setItem(14, createDisplayItem(Material.EMERALD, lang.equals("TR") ? "Zümrüt" : "Emerald", 120, 60, lang));
        inv.setItem(15, createDisplayItem(Material.NETHERITE_INGOT, lang.equals("TR") ? "Netherite" : "Netherite", 1500, 750, lang));
        inv.setItem(16, createDisplayItem(Material.LAPIS_LAZULI, "Lapis", 15, 7, lang));
        inv.setItem(19, createDisplayItem(Material.REDSTONE, "Redstone", 10, 5, lang));
        inv.setItem(20, createDisplayItem(Material.COPPER_INGOT, lang.equals("TR") ? "Bakır" : "Copper", 20, 10, lang));
        setBackButton(inv, lang);
        player.openInventory(inv);
    }

    private void openBlocksMenu(Player player, String lang) {
        Inventory inv = Bukkit.createInventory(null, 36, lang.equals("TR") ? "İnşaat Blokları" : "Building Blocks");
        inv.setItem(10, createDisplayItem(Material.DIRT, lang.equals("TR") ? "Toprak" : "Dirt", 5, 1, lang));
        inv.setItem(11, createDisplayItem(Material.COBBLESTONE, lang.equals("TR") ? "Kırıktaş" : "Cobblestone", 5, 2, lang));
        inv.setItem(12, createDisplayItem(Material.STONE, lang.equals("TR") ? "Taş" : "Stone", 10, 5, lang));
        inv.setItem(13, createDisplayItem(Material.OAK_LOG, lang.equals("TR") ? "Meşe Odunu" : "Oak Log", 20, 10, lang));
        inv.setItem(14, createDisplayItem(Material.BIRCH_LOG, lang.equals("TR") ? "Huş Odunu" : "Birch Log", 20, 10, lang));
        inv.setItem(15, createDisplayItem(Material.SPRUCE_LOG, lang.equals("TR") ? "Ladin Odunu" : "Spruce Log", 20, 10, lang));
        inv.setItem(16, createDisplayItem(Material.SAND, lang.equals("TR") ? "Kum" : "Sand", 8, 3, lang));
        inv.setItem(19, createDisplayItem(Material.GLASS, lang.equals("TR") ? "Cam" : "Glass", 15, 5, lang));
        inv.setItem(20, createDisplayItem(Material.OBSIDIAN, lang.equals("TR") ? "Obsidyen" : "Obsidian", 200, 100, lang));
        setBackButton(inv, lang);
        player.openInventory(inv);
    }

    private void openFoodMenu(Player player, String lang) {
        Inventory inv = Bukkit.createInventory(null, 36, lang.equals("TR") ? "Yiyecekler" : "Food");
        inv.setItem(10, createDisplayItem(Material.CARROT, lang.equals("TR") ? "Havuç" : "Carrot", 5, 2, lang));
        inv.setItem(11, createDisplayItem(Material.POTATO, lang.equals("TR") ? "Patates" : "Potato", 5, 2, lang));
        inv.setItem(12, createDisplayItem(Material.BREAD, lang.equals("TR") ? "Ekmek" : "Bread", 10, 5, lang));
        inv.setItem(13, createDisplayItem(Material.APPLE, lang.equals("TR") ? "Elma" : "Apple", 10, 5, lang));
        inv.setItem(14, createDisplayItem(Material.MELON_SLICE, lang.equals("TR") ? "Karpuz" : "Melon", 5, 2, lang));
        inv.setItem(15, createDisplayItem(Material.COOKED_BEEF, lang.equals("TR") ? "Pişmiş Et" : "Cooked Beef", 25, 12, lang));
        inv.setItem(16, createDisplayItem(Material.COOKED_CHICKEN, lang.equals("TR") ? "Pişmiş Tavuk" : "Cooked Chicken", 15, 7, lang));
        inv.setItem(19, createDisplayItem(Material.GOLDEN_APPLE, lang.equals("TR") ? "Altın Elma" : "Golden Apple", 250, 100, lang));
        setBackButton(inv, lang);
        player.openInventory(inv);
    }

    private void openDropsMenu(Player player, String lang) {
        Inventory inv = Bukkit.createInventory(null, 36, lang.equals("TR") ? "Canavar Düşenleri" : "Mob Drops");
        inv.setItem(10, createDisplayItem(Material.ROTTEN_FLESH, lang.equals("TR") ? "Çürük Et" : "Rotten Flesh", 5, 2, lang));
        inv.setItem(11, createDisplayItem(Material.BONE, lang.equals("TR") ? "Kemik" : "Bone", 10, 5, lang));
        inv.setItem(12, createDisplayItem(Material.STRING, lang.equals("TR") ? "İp" : "String", 10, 5, lang));
        inv.setItem(13, createDisplayItem(Material.SPIDER_EYE, lang.equals("TR") ? "Örümcek Gözü" : "Spider Eye", 15, 5, lang));
        inv.setItem(14, createDisplayItem(Material.GUNPOWDER, lang.equals("TR") ? "Barut" : "Gunpowder", 25, 10, lang));
        inv.setItem(15, createDisplayItem(Material.ENDER_PEARL, lang.equals("TR") ? "Ender İncisi" : "Ender Pearl", 75, 30, lang));
        inv.setItem(16, createDisplayItem(Material.SLIME_BALL, lang.equals("TR") ? "Balçık Topu" : "Slimeball", 40, 20, lang));
        inv.setItem(19, createDisplayItem(Material.BLAZE_ROD, lang.equals("TR") ? "Blaze Çubuğu" : "Blaze Rod", 100, 50, lang));
        setBackButton(inv, lang);
        player.openInventory(inv);
    }

    private void openArmorMenu(Player player, String lang) {
        Inventory inv = Bukkit.createInventory(null, 36, lang.equals("TR") ? "Zırhlar" : "Armor");
        inv.setItem(10, createDisplayItem(Material.IRON_HELMET, lang.equals("TR") ? "Demir Kask" : "Iron Helmet", 250, 125, lang));
        inv.setItem(11, createDisplayItem(Material.IRON_CHESTPLATE, lang.equals("TR") ? "Demir Göğüslük" : "Iron Chestplate", 400, 200, lang));
        inv.setItem(12, createDisplayItem(Material.IRON_LEGGINGS, lang.equals("TR") ? "Demir Pantolon" : "Iron Leggings", 350, 175, lang));
        inv.setItem(13, createDisplayItem(Material.IRON_BOOTS, lang.equals("TR") ? "Demir Bot" : "Iron Boots", 200, 100, lang));
        inv.setItem(19, createDisplayItem(Material.DIAMOND_HELMET, lang.equals("TR") ? "Elmas Kask" : "Diamond Helmet", 500, 250, lang));
        inv.setItem(20, createDisplayItem(Material.DIAMOND_CHESTPLATE, lang.equals("TR") ? "Elmas Göğüslük" : "Diamond Chestplate", 800, 400, lang));
        inv.setItem(21, createDisplayItem(Material.DIAMOND_LEGGINGS, lang.equals("TR") ? "Elmas Pantolon" : "Diamond Leggings", 700, 350, lang));
        inv.setItem(22, createDisplayItem(Material.DIAMOND_BOOTS, lang.equals("TR") ? "Elmas Bot" : "Diamond Boots", 400, 200, lang));
        setBackButton(inv, lang);
        player.openInventory(inv);
    }

    private void openToolsMenu(Player player, String lang) {
        Inventory inv = Bukkit.createInventory(null, 36, lang.equals("TR") ? "Aletler & Silahlar" : "Tools & Weapons");
        inv.setItem(10, createDisplayItem(Material.IRON_SWORD, lang.equals("TR") ? "Demir Kılıç" : "Iron Sword", 200, 100, lang));
        inv.setItem(11, createDisplayItem(Material.IRON_PICKAXE, lang.equals("TR") ? "Demir Kazma" : "Iron Pickaxe", 300, 150, lang));
        inv.setItem(12, createDisplayItem(Material.IRON_AXE, lang.equals("TR") ? "Demir Balta" : "Iron Axe", 300, 150, lang));
        inv.setItem(13, createDisplayItem(Material.IRON_SHOVEL, lang.equals("TR") ? "Demir Kürek" : "Iron Shovel", 100, 50, lang));
        inv.setItem(19, createDisplayItem(Material.DIAMOND_SWORD, lang.equals("TR") ? "Elmas Kılıç" : "Diamond Sword", 400, 200, lang));
        inv.setItem(20, createDisplayItem(Material.DIAMOND_PICKAXE, lang.equals("TR") ? "Elmas Kazma" : "Diamond Pickaxe", 600, 300, lang));
        inv.setItem(21, createDisplayItem(Material.DIAMOND_AXE, lang.equals("TR") ? "Elmas Balta" : "Diamond Axe", 600, 300, lang));
        inv.setItem(22, createDisplayItem(Material.DIAMOND_SHOVEL, lang.equals("TR") ? "Elmas Kürek" : "Diamond Shovel", 200, 100, lang));
        inv.setItem(15, createDisplayItem(Material.BOW, lang.equals("TR") ? "Yay" : "Bow", 150, 75, lang));
        inv.setItem(24, createDisplayItem(Material.ARROW, lang.equals("TR") ? "Ok" : "Arrow", 5, 2, lang));
        setBackButton(inv, lang);
        player.openInventory(inv);
    }

    private ItemStack createDisplayItem(Material mat, String name, double buy, double sell, String lang) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + name);
        List<String> lore = new ArrayList<>();

        // Sembolü dinamik olarak Config'den çeker
        String sym = plugin.currencySymbol;

        if (lang.equals("TR")) {
            lore.add(ChatColor.GRAY + "Alış: " + ChatColor.GREEN + sym + buy);
            lore.add(ChatColor.GRAY + "Satış: " + ChatColor.RED + sym + sell);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Sol Tık: " + ChatColor.WHITE + "1 Tane Al");
            lore.add(ChatColor.YELLOW + "Sağ Tık: " + ChatColor.WHITE + "1 Tane Sat");
            lore.add(ChatColor.YELLOW + "Shift + Sağ Tık: " + ChatColor.WHITE + "Hepsini Sat");
        } else {
            lore.add(ChatColor.GRAY + "Buy: " + ChatColor.GREEN + sym + buy);
            lore.add(ChatColor.GRAY + "Sell: " + ChatColor.RED + sym + sell);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Left Click: " + ChatColor.WHITE + "Buy 1");
            lore.add(ChatColor.YELLOW + "Right Click: " + ChatColor.WHITE + "Sell 1");
            lore.add(ChatColor.YELLOW + "Shift + Right Click: " + ChatColor.WHITE + "Sell All");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}