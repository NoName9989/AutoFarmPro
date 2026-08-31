package com.autofarm;

import com.autofarm.commands.AutoFarmCommand;
import com.autofarm.config.ConfigManager;
import com.autofarm.listeners.FishingListener;
import com.autofarm.managers.PlayerStateManager;
import com.autofarm.managers.StatsManager;
import com.autofarm.tasks.AutoFarmTask;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoFarmPlugin extends JavaPlugin {

    private static AutoFarmPlugin instance;

    private ConfigManager configManager;
    private PlayerStateManager playerStateManager;
    private StatsManager statsManager;
    private AutoFarmTask autoFarmTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.playerStateManager = new PlayerStateManager();
        this.statsManager = new StatsManager();

        AutoFarmCommand commandExecutor = new AutoFarmCommand(this);
        PluginCommand cmd = getCommand("autofarm");
        if (cmd != null) {
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(commandExecutor);
        } else {
            getLogger().warning("Khong tim thay lenh 'autofarm' trong plugin.yml!");
        }

        getServer().getPluginManager().registerEvents(new FishingListener(this), this);

        startFarmTask();

        getLogger().info("AutoFarmPro da duoc kich hoat thanh cong!");
    }

    @Override
    public void onDisable() {
        if (autoFarmTask != null) {
            autoFarmTask.cancel();
        }
        getLogger().info("AutoFarmPro da bi vo hieu hoa!");
    }

    /**
     * Khoi dong lai task quet farm (goi lai khi reload de ap dung scan-interval-ticks moi).
     */
    public void startFarmTask() {
        if (autoFarmTask != null) {
            autoFarmTask.cancel();
        }
        autoFarmTask = new AutoFarmTask(this);
        long interval = Math.max(1L, configManager.getScanIntervalTicks());
        autoFarmTask.runTaskTimer(this, 20L, interval);
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        startFarmTask();
    }

    public static AutoFarmPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}
