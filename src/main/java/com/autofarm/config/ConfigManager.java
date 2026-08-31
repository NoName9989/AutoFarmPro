package com.autofarm.config;

import com.autofarm.AutoFarmPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Doc va cache toan bo gia tri tu config.yml. Goi reload() de nap lai sau khi
 * plugin.reloadConfig() duoc goi (vd tu lenh /autofarm reload).
 */
public class ConfigManager {

    private final AutoFarmPlugin plugin;

    private Set<String> enabledWorlds;
    private long scanIntervalTicks;
    private int maxBlocksPerScan;

    private boolean farmEnabled;
    private int farmRadius;
    private int farmRangeY;
    private boolean autoHarvest;
    private boolean autoPlant;
    private boolean autoPickup;
    private boolean particlesEnabled;
    private boolean soundEnabled;
    private final Set<Material> enabledCrops = EnumSet.noneOf(Material.class);

    private boolean fishingEnabled;
    private boolean autoRecast;
    private int reelDelayMin;
    private int reelDelayMax;
    private int recastDelayTicks;
    private boolean breakRodWhenBroken;

    private final Map<String, String> messages = new HashMap<>();

    public ConfigManager(AutoFarmPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration cfg = plugin.getConfig();

        enabledWorlds = new HashSet<>(cfg.getStringList("general.enabled-worlds"));
        scanIntervalTicks = cfg.getLong("general.scan-interval-ticks", 20L);
        maxBlocksPerScan = cfg.getInt("general.max-blocks-per-scan", 200);

        farmEnabled = cfg.getBoolean("farm.enabled", true);
        farmRadius = cfg.getInt("farm.radius", 1);
        farmRangeY = cfg.getInt("farm.range-y", 1);
        autoHarvest = cfg.getBoolean("farm.auto-harvest", true);
        autoPlant = cfg.getBoolean("farm.auto-plant", true);
        autoPickup = cfg.getBoolean("farm.auto-pickup", true);
        particlesEnabled = cfg.getBoolean("farm.effects.particles", true);
        soundEnabled = cfg.getBoolean("farm.effects.sound", true);

        enabledCrops.clear();
        ConfigurationSection cropsSection = cfg.getConfigurationSection("farm.crops");
        if (cropsSection != null) {
            for (String key : cropsSection.getKeys(false)) {
                if (cropsSection.getBoolean(key)) {
                    try {
                        enabledCrops.add(Material.valueOf(key.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().warning("Loai cay trong khong hop le trong config.yml: " + key);
                    }
                }
            }
        }

        fishingEnabled = cfg.getBoolean("fishing.enabled", true);
        autoRecast = cfg.getBoolean("fishing.auto-recast", true);
        reelDelayMin = Math.max(1, cfg.getInt("fishing.reel-delay-min-ticks", 3));
        reelDelayMax = Math.max(reelDelayMin, cfg.getInt("fishing.reel-delay-max-ticks", 10));
        recastDelayTicks = cfg.getInt("fishing.recast-delay-ticks", 10);
        breakRodWhenBroken = cfg.getBoolean("fishing.break-rod-when-broken", true);

        messages.clear();
        ConfigurationSection msgSection = cfg.getConfigurationSection("messages");
        if (msgSection != null) {
            for (String key : msgSection.getKeys(false)) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', msgSection.getString(key, "")));
            }
        }
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.isEmpty() || enabledWorlds.contains(worldName);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }

    public String getPrefix() {
        return messages.getOrDefault("prefix", "");
    }

    public boolean isCropEnabled(Material material) {
        return enabledCrops.contains(material);
    }

    public long getScanIntervalTicks() { return scanIntervalTicks; }
    public int getMaxBlocksPerScan() { return maxBlocksPerScan; }
    public boolean isFarmEnabled() { return farmEnabled; }
    public int getFarmRadius() { return farmRadius; }
    public int getFarmRangeY() { return farmRangeY; }
    public boolean isAutoHarvest() { return autoHarvest; }
    public boolean isAutoPlant() { return autoPlant; }
    public boolean isAutoPickup() { return autoPickup; }
    public boolean isParticlesEnabled() { return particlesEnabled; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isFishingEnabled() { return fishingEnabled; }
    public boolean isAutoRecast() { return autoRecast; }
    public int getReelDelayMin() { return reelDelayMin; }
    public int getReelDelayMax() { return reelDelayMax; }
    public int getRecastDelayTicks() { return recastDelayTicks; }
    public boolean isBreakRodWhenBroken() { return breakRodWhenBroken; }
}
