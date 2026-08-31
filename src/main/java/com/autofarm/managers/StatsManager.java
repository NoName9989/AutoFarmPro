package com.autofarm.managers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsManager {

    private final ConcurrentHashMap<UUID, AtomicInteger> harvestCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, AtomicInteger> fishCount = new ConcurrentHashMap<>();

    public void incrementHarvest(UUID uuid) {
        harvestCount.computeIfAbsent(uuid, k -> new AtomicInteger()).incrementAndGet();
    }

    public void incrementFish(UUID uuid) {
        fishCount.computeIfAbsent(uuid, k -> new AtomicInteger()).incrementAndGet();
    }

    public int getHarvestCount(UUID uuid) {
        AtomicInteger counter = harvestCount.get(uuid);
        return counter == null ? 0 : counter.get();
    }

    public int getFishCount(UUID uuid) {
        AtomicInteger counter = fishCount.get(uuid);
        return counter == null ? 0 : counter.get();
    }
}
