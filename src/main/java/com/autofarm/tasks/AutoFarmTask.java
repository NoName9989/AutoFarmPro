package com.autofarm.tasks;

import com.autofarm.AutoFarmPlugin;
import com.autofarm.utils.CropUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Chay dinh ky (theo general.scan-interval-ticks). Voi moi nguoi choi dang bat
 * auto-farm, quet 1 khoi hop (radius x radius x range-y) quanh vi tri dung va
 * thu hoach / trong lai cac block cay trong trong vung do.
 */
public class AutoFarmTask extends BukkitRunnable {

    private final AutoFarmPlugin plugin;

    public AutoFarmTask(AutoFarmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().isFarmEnabled()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!plugin.getPlayerStateManager().isFarmEnabled(player.getUniqueId())) continue;
            if (!player.hasPermission("autofarm.use")) continue;
            if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) continue;

            scanAndProcess(player);
        }
    }

    private void scanAndProcess(Player player) {
        int radius = Math.max(0, plugin.getConfigManager().getFarmRadius());
        int rangeY = Math.max(0, plugin.getConfigManager().getFarmRangeY());
        int maxPerScan = plugin.getConfigManager().getMaxBlocksPerScan();

        Location base = player.getLocation();
        int processed = 0;

        // TODO (mo rong): neu can gioi han theo vung dat rieng cua nguoi choi,
        // day la noi chen kiem tra WorldGuard / claim-plugin truoc khi xu ly block.

        outer:
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -rangeY; y <= rangeY; y++) {
                    Block block = base.getWorld().getBlockAt(
                            base.getBlockX() + x,
                            base.getBlockY() + y,
                            base.getBlockZ() + z
                    );

                    boolean acted = CropUtils.tryHarvest(block, player, plugin);
                    if (!acted) {
                        acted = CropUtils.tryAutoPlant(block, player, plugin);
                    }

                    if (acted) {
                        processed++;
                        if (processed >= maxPerScan) break outer;
                    }
                }
            }
        }
    }
}
