package com.autofarm.listeners;

import com.autofarm.AutoFarmPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * Auto-fishing: khi phao cau bi keo xuong (State.BITE), sau 1 khoang tre ngau
 * nhien se tu dong "giat can" bang cach lay loot tu LootTable cau ca thuc su
 * cua vanilla (khong can gia lap click chuot), roi tu tha can lai neu bat.
 */
public class FishingListener implements Listener {

    private final AutoFarmPlugin plugin;
    private final Random random = new Random();

    public FishingListener(AutoFarmPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.BITE) return;

        Player player = event.getPlayer();
        if (!plugin.getConfigManager().isFishingEnabled()) return;
        if (!plugin.getPlayerStateManager().isFishEnabled(player.getUniqueId())) return;
        if (!player.hasPermission("autofarm.use")) return;

        FishHook hook = event.getHook();
        Location hookLocation = hook.getLocation();

        int min = plugin.getConfigManager().getReelDelayMin();
        int max = plugin.getConfigManager().getReelDelayMax();
        int delay = (min >= max) ? min : min + random.nextInt(max - min + 1);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && hook.isValid()) {
                reelIn(player, hook, hookLocation);
            }
        }, Math.max(1, delay));
    }

    private void reelIn(Player player, FishHook hook, Location hookLocation) {
        LootTable lootTable = LootTables.FISHING.getLootTable();
        LootContext context = new LootContext.Builder(hookLocation).build();
        Collection<ItemStack> loot = lootTable.populateLoot(random, context);

        for (ItemStack item : loot) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            for (ItemStack extra : leftover.values()) {
                hookLocation.getWorld().dropItemNaturally(hookLocation, extra);
            }
        }

        player.giveExp(random.nextInt(3) + 1);
        hookLocation.getWorld().playSound(hookLocation, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
        hookLocation.getWorld().spawnParticle(Particle.WATER_SPLASH, hookLocation, 15, 0.2, 0.1, 0.2);

        hook.remove();
        plugin.getStatsManager().incrementFish(player.getUniqueId());

        damageRod(player);

        if (plugin.getConfigManager().isAutoRecast() && plugin.getPlayerStateManager().isFishEnabled(player.getUniqueId())) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> tryRecast(player),
                    Math.max(1, plugin.getConfigManager().getRecastDelayTicks()));
        }
    }

    private void damageRod(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        PlayerInventory inv = player.getInventory();
        boolean mainHand = inv.getItemInMainHand().getType() == Material.FISHING_ROD;
        boolean offHand = !mainHand && inv.getItemInOffHand().getType() == Material.FISHING_ROD;
        if (!mainHand && !offHand) return;

        ItemStack rod = mainHand ? inv.getItemInMainHand() : inv.getItemInOffHand();
        if (!(rod.getItemMeta() instanceof Damageable meta)) return;
        if (meta.isUnbreakable()) return;

        meta.setDamage(meta.getDamage() + 1);
        boolean broken = meta.getDamage() >= rod.getType().getMaxDurability();

        if (broken && plugin.getConfigManager().isBreakRodWhenBroken()) {
            if (mainHand) inv.setItemInMainHand(new ItemStack(Material.AIR));
            else inv.setItemInOffHand(new ItemStack(Material.AIR));

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("rod-broken"));
            plugin.getPlayerStateManager().setFishEnabled(player.getUniqueId(), false);
        } else {
            rod.setItemMeta(meta);
            if (mainHand) inv.setItemInMainHand(rod);
            else inv.setItemInOffHand(rod);
        }
    }

    private ItemStack getFishingRod(Player player) {
        PlayerInventory inv = player.getInventory();
        if (inv.getItemInMainHand().getType() == Material.FISHING_ROD) return inv.getItemInMainHand();
        if (inv.getItemInOffHand().getType() == Material.FISHING_ROD) return inv.getItemInOffHand();
        return null;
    }

    private void tryRecast(Player player) {
        if (!player.isOnline()) return;
        if (!plugin.getPlayerStateManager().isFishEnabled(player.getUniqueId())) return;
        if (getFishingRod(player) == null) return;

        player.launchProjectile(FishHook.class);
    }
}
