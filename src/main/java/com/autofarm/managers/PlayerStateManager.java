package com.autofarm.managers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Luu trang thai bat/tat tinh nang cho tung nguoi choi trong bo nho (RAM).
 * Se reset khi restart server - xem README neu can luu vinh vien qua file/database.
 */
public class PlayerStateManager {

    private final Set<UUID> farmEnabled = ConcurrentHashMap.newKeySet();
    private final Set<UUID> fishEnabled = ConcurrentHashMap.newKeySet();

    public boolean isFarmEnabled(UUID uuid) {
        return farmEnabled.contains(uuid);
    }

    /**
     * Dao trang thai bat/tat. Tra ve trang thai MOI sau khi dao (true = vua bat).
     */
    public boolean toggleFarm(UUID uuid) {
        if (!farmEnabled.add(uuid)) {
            farmEnabled.remove(uuid);
            return false;
        }
        return true;
    }

    public void setFarmEnabled(UUID uuid, boolean value) {
        if (value) farmEnabled.add(uuid);
        else farmEnabled.remove(uuid);
    }

    public boolean isFishEnabled(UUID uuid) {
        return fishEnabled.contains(uuid);
    }

    public boolean toggleFish(UUID uuid) {
        if (!fishEnabled.add(uuid)) {
            fishEnabled.remove(uuid);
            return false;
        }
        return true;
    }

    public void setFishEnabled(UUID uuid, boolean value) {
        if (value) fishEnabled.add(uuid);
        else fishEnabled.remove(uuid);
    }
}
