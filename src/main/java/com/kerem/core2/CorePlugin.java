package com.kerem.core2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CorePlugin extends JavaPlugin {

    public HashMap<UUID, String> playerLangs = new HashMap<>();
    public HashMap<UUID, Double> playerMoney = new HashMap<>();
    public HashMap<UUID, UUID> tpaRequests = new HashMap<>();
    public HashMap<UUID, Location> playerHomes = new HashMap<>();
    public List<UUID> hiddenScoreboards = new ArrayList<>();

    // Config'den çekilecek ekonomi ayarları
    public String currencySymbol;
    public double startingBalance;
    public double maxMoney;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Ayarları yükle
        currencySymbol = getConfig().getString("currency-symbol", "$");
        startingBalance = getConfig().getDouble("starting-balance", 100.0);
        maxMoney = getConfig().getDouble("max-money", 1000000.0);

        getLogger().info("AtomCore is starting...");

        getCommand("lang").setExecutor(new LangCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("tpaccept").setExecutor(new TpaCommand(this));
        getCommand("tpdeny").setExecutor(new TpaCommand(this));
        getCommand("sethome").setExecutor(new HomeCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sb").setExecutor(new SBCommand(this)); // Senin düzelttiğin SBCommand!
        getCommand("help").setExecutor(new HelpCommand(this));

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, 20L);

        getLogger().info("AtomCore successfully enabled!");
    }

    public void updateScoreboard(Player player) {
        if (hiddenScoreboards.contains(player.getUniqueId())) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            return;
        }

        String lang = playerLangs.getOrDefault(player.getUniqueId(), "EN");

        // Başlangıç parasını artık config'den (startingBalance) çekiyor
        playerMoney.putIfAbsent(player.getUniqueId(), startingBalance);
        double money = playerMoney.get(player.getUniqueId());

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        String configTitle = getConfig().getString("scoreboard-title", "&6&lAtomCore");
        String titleText = ChatColor.translateAlternateColorCodes('&', configTitle);

        Objective obj = board.registerNewObjective("AtomCoreSB", "dummy", titleText);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int kills = player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS);
        int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);
        int ping = player.getPing();

        obj.getScore(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------").setScore(10);
        obj.getScore(ChatColor.YELLOW + "👤 " + (lang.equals("TR") ? "Profil:" : "Profile:")).setScore(9);
        obj.getScore(ChatColor.WHITE + " ▫ " + player.getName()).setScore(8);
        obj.getScore(ChatColor.WHITE + " ▫ Ping: " + ChatColor.GREEN + ping + "ms").setScore(7);
        obj.getScore("  ").setScore(6);
        obj.getScore(ChatColor.YELLOW + "💰 " + (lang.equals("TR") ? "Ekonomi:" : "Economy:")).setScore(5);

        // Sembolü config'den çekerek yazdırıyor (Örn: $100 veya 100 TL)
        obj.getScore(ChatColor.WHITE + " ▫ " + (lang.equals("TR") ? "Para: " : "Money: ") + ChatColor.GREEN + currencySymbol + money).setScore(4);

        obj.getScore(" ").setScore(3);
        obj.getScore(ChatColor.YELLOW + "⚔ " + (lang.equals("TR") ? "İstatistik:" : "Combat:")).setScore(2);
        obj.getScore(ChatColor.WHITE + " ▫ K/D: " + ChatColor.RED + kills + ChatColor.GRAY + " / " + ChatColor.RED + deaths).setScore(1);
        obj.getScore(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-------------------- ").setScore(0);

        player.setScoreboard(board);
    }
}