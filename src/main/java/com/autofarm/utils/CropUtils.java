package com.autofarm.utils;

import com.autofarm.AutoFarmPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tat ca logic thu hoach / trong lai / trong moi. Duoc goi tu AutoFarmTask
 * cho tung block trong vung quet cua nguoi choi.
 */
public final class CropUtils {

    // Cac loai cay dung chung co che Ageable (lon dan theo "age", reset ve 0 la hoi sinh)
    private static final Set<Material> AGEABLE_CROPS = EnumSet.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.NETHER_WART,
            Material.COCOA
    );

    // Cac loai cay moc "xep chong" theo chieu doc (chi can giu lai 1 block goc)
    private static final Set<Material> STACKING_CROPS = EnumSet.of(
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.BAMBOO
    );

    /**
     * crop  = block cay trong (vd WHEAT)
     * seed  = vat pham can co trong tui de trong (vd WHEAT_SEEDS)
     * soil  = block nen can co ben duoi (vd FARMLAND)
     */
    private record PlantablePair(Material crop, Material seed, Material soil) {}

    private static final List<PlantablePair> PLANTABLE = List.of(
            new PlantablePair(Material.WHEAT, Material.WHEAT_SEEDS, Material.FARMLAND),
            new PlantablePair(Material.CARROTS, Material.CARROT, Material.FARMLAND),
            new PlantablePair(Material.POTATOES, Material.POTATO, Material.FARMLAND),
            new PlantablePair(Material.BEETROOTS, Material.BEETROOT_SEEDS, Material.FARMLAND),
            // Nether Wart dac biet: item va block dung chung ten trong Bukkit (NETHER_WART)
            new PlantablePair(Material.NETHER_WART, Material.NETHER_WART, Material.SOUL_SAND)
    );

    private CropUtils() {}

    /**
     * Thu tim va thu hoach 1 block neu no la cay da chin. Tra ve true neu da xu ly.
     */
    public static boolean tryHarvest(Block block, Player player, AutoFarmPlugin plugin) {
        if (!plugin.getConfigManager().isAutoHarvest()) return false;

        Material type = block.getType();
        if (!plugin.getConfigManager().isCropEnabled(type)) return false;

        if (AGEABLE_CROPS.contains(type)) {
            return harvestAgeable(block, player, plugin);
        }
        if (STACKING_CROPS.contains(type)) {
            return harvestStacking(block, type, player, plugin);
        }
        if (type == Material.MELON || type == Material.PUMPKIN) {
            return harvestFruit(block, player, plugin);
        }
        return false;
    }

    /**
     * Thu trong hat giong tu tui do nguoi choi len 1 block dat trong (farmland/soul sand
     * co khoang trong ben tren). Tra ve true neu da trong thanh cong.
     */
    public static boolean tryAutoPlant(Block block, Player player, AutoFarmPlugin plugin) {
        if (!plugin.getConfigManager().isAutoPlant()) return false;

        Block above = block.getRelative(0, 1, 0);
        if (above.getType() != Material.AIR) return false;

        Material soilType = block.getType();
        Material heldType = player.getInventory().getItemInMainHand().getType();

        // Uu tien hat giong dang cam tren tay
        for (PlantablePair pair : PLANTABLE) {
            if (pair.soil() == soilType && pair.seed() == heldType
                    && plugin.getConfigManager().isCropEnabled(pair.crop())
                    && consumeSeed(player, pair.seed())) {
                above.setType(pair.crop(), false);
                onHarvested(above.getLocation(), player, plugin);
                return true;
            }
        }

        // Neu khong, tim trong tui do
        for (PlantablePair pair : PLANTABLE) {
            if (pair.soil() != soilType) continue;
            if (!plugin.getConfigManager().isCropEnabled(pair.crop())) continue;
            if (consumeSeed(player, pair.seed())) {
                above.setType(pair.crop(), false);
                onHarvested(above.getLocation(), player, plugin);
                return true;
            }
        }

        return false;
    }

    private static boolean harvestAgeable(Block block, Player player, AutoFarmPlugin plugin) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Ageable ageable)) return false;
        if (ageable.getAge() < ageable.getMaximumAge()) return false;

        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
        giveOrDrop(player, block.getLocation(), drops, plugin);

        // Reset ve age 0 thay vi pha block -> hoi sinh tuc thi, giu nguyen huong
        // (quan trong voi Cocoa vi no can Directional gan voi khuc go)
        ageable.setAge(0);
        block.setBlockData(ageable, false);

        onHarvested(block.getLocation(), player, plugin);
        return true;
    }

    private static boolean harvestStacking(Block block, Material type, Player player, AutoFarmPlugin plugin) {
        if (block.getRelative(0, -1, 0).getType() == type) {
            return false; // Khong phai block goc cua cum, se duoc xu ly tu goc
        }

        Block current = block.getRelative(0, 1, 0);
        List<ItemStack> allDrops = new ArrayList<>();
        boolean harvested = false;

        while (current.getType() == type) {
            allDrops.addAll(current.getDrops());
            Block next = current.getRelative(0, 1, 0);
            current.setType(Material.AIR, false);
            current = next;
            harvested = true;
        }

        if (harvested) {
            giveOrDrop(player, block.getLocation(), allDrops, plugin);
            onHarvested(block.getLocation(), player, plugin);
        }
        return harvested;
    }

    private static boolean harvestFruit(Block block, Player player, AutoFarmPlugin plugin) {
        // Melon/Pumpkin: chi pha qua, cuong (stem) van con va se tu moc qua khac
        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
        giveOrDrop(player, block.getLocation(), drops, plugin);
        block.setType(Material.AIR, false);

        onHarvested(block.getLocation(), player, plugin);
        return true;
    }

    private static boolean consumeSeed(Player player, Material seedType) {
        if (player.getGameMode() == GameMode.CREATIVE) return true;

        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();
        if (main.getType() == seedType) {
            if (main.getAmount() <= 1) {
                inv.setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                main.setAmount(main.getAmount() - 1);
                inv.setItemInMainHand(main);
            }
            return true;
        }

        ItemStack[] storage = inv.getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            ItemStack item = storage[i];
            if (item != null && item.getType() == seedType) {
                if (item.getAmount() <= 1) {
                    storage[i] = null;
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                inv.setStorageContents(storage);
                return true;
            }
        }
        return false;
    }

    private static void giveOrDrop(Player player, Location location, Collection<ItemStack> drops, AutoFarmPlugin plugin) {
        for (ItemStack item : drops) {
            if (item == null || item.getType().isAir()) continue;

            if (plugin.getConfigManager().isAutoPickup()) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack extra : leftover.values()) {
                    location.getWorld().dropItemNaturally(location, extra);
                }
            } else {
                location.getWorld().dropItemNaturally(location, item);
            }
        }
    }

    private static void onHarvested(Location location, Player player, AutoFarmPlugin plugin) {
        plugin.getStatsManager().incrementHarvest(player.getUniqueId());

        if (plugin.getConfigManager().isParticlesEnabled()) {
            // Luu y: neu build voi Paper API 1.21+, ten particle nay co the doi thanh HAPPY_VILLAGER
            location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                    location.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2);
        }
        if (plugin.getConfigManager().isSoundEnabled()) {
            location.getWorld().playSound(location, Sound.BLOCK_CROP_BREAK, 0.6f, 1.0f);
        }
    }
}
