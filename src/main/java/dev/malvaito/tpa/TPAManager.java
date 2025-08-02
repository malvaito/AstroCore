package dev.malvaito.tpa;

import dev.malvaito.AstroCore;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {

    private final Map<UUID, UUID> tpaRequests; // Target UUID -> Requester UUID
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, BukkitTask> teleportTasks;
    private final Map<UUID, Location> playerLocations;

    public TPAManager(AstroCore plugin) {
        this.tpaRequests = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.teleportTasks = new HashMap<>();
        this.playerLocations = new HashMap<>();
    }

    public Map<UUID, UUID> getTpaRequests() {
        return tpaRequests;
    }

    public Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }

    public Map<UUID, BukkitTask> getTeleportTasks() {
        return teleportTasks;
    }

    public Map<UUID, Location> getPlayerLocations() {
        return playerLocations;
    }

    public void addCooldown(UUID playerUuid, long durationMillis) {
        cooldowns.put(playerUuid, System.currentTimeMillis() + durationMillis);
    }

    public boolean isOnCooldown(UUID playerUuid) {
        return cooldowns.containsKey(playerUuid) && cooldowns.get(playerUuid) > System.currentTimeMillis();
    }

    public long getRemainingCooldown(UUID playerUuid) {
        if (isOnCooldown(playerUuid)) {
            return (cooldowns.get(playerUuid) - System.currentTimeMillis()) / 1000;
        }
        return 0;
    }

    public void addTeleportTask(UUID playerUuid, BukkitTask task, Location location) {
        teleportTasks.put(playerUuid, task);
        playerLocations.put(playerUuid, location);
    }

    public void cancelTeleport(UUID playerUuid) {
        if (teleportTasks.containsKey(playerUuid)) {
            teleportTasks.get(playerUuid).cancel();
            teleportTasks.remove(playerUuid);
            playerLocations.remove(playerUuid);
        }
    }
}