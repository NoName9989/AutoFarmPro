package com.autofarm.commands;

import com.autofarm.AutoFarmPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class AutoFarmCommand implements CommandExecutor, TabCompleter {

    private final AutoFarmPlugin plugin;
    private static final List<String> SUBCOMMANDS = List.of("farm", "fish", "stats", "reload", "help");

    public AutoFarmCommand(AutoFarmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "farm" -> handleFarmToggle(sender);
            case "fish" -> handleFishToggle(sender);
            case "stats" -> handleStats(sender);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleFarmToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lenh nay chi danh cho nguoi choi.");
            return;
        }
        if (!player.hasPermission("autofarm.use")) {
            player.sendMessage(prefixed("no-permission"));
            return;
        }
        boolean enabled = plugin.getPlayerStateManager().toggleFarm(player.getUniqueId());
        player.sendMessage(prefixed(enabled ? "farm-enabled" : "farm-disabled"));
    }

    private void handleFishToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lenh nay chi danh cho nguoi choi.");
            return;
        }
        if (!player.hasPermission("autofarm.use")) {
            player.sendMessage(prefixed("no-permission"));
            return;
        }
        boolean enabled = plugin.getPlayerStateManager().toggleFish(player.getUniqueId());
        player.sendMessage(prefixed(enabled ? "fish-enabled" : "fish-disabled"));
    }

    private void handleStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lenh nay chi danh cho nguoi choi.");
            return;
        }
        int harvests = plugin.getStatsManager().getHarvestCount(player.getUniqueId());
        int fishes = plugin.getStatsManager().getFishCount(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "=== Thong ke AutoFarm ===");
        player.sendMessage(ChatColor.YELLOW + "Cay da thu hoach: " + ChatColor.WHITE + harvests);
        player.sendMessage(ChatColor.YELLOW + "Ca da cau duoc: " + ChatColor.WHITE + fishes);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("autofarm.admin")) {
            sender.sendMessage(prefixed("no-permission"));
            return;
        }
        plugin.reload();
        sender.sendMessage(prefixed("reload-success"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== AutoFarmPro ===");
        sender.sendMessage(ChatColor.YELLOW + "/autofarm farm " + ChatColor.WHITE + "- Bat/tat tu dong lam vuon");
        sender.sendMessage(ChatColor.YELLOW + "/autofarm fish " + ChatColor.WHITE + "- Bat/tat tu dong cau ca");
        sender.sendMessage(ChatColor.YELLOW + "/autofarm stats " + ChatColor.WHITE + "- Xem thong ke ca nhan");
        if (sender.hasPermission("autofarm.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/autofarm reload " + ChatColor.WHITE + "- Tai lai cau hinh");
        }
    }

    private String prefixed(String key) {
        return plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage(key);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
