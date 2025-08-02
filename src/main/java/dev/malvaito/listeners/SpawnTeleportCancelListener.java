package dev.malvaito.listeners;

import dev.malvaito.spawn.SpawnCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;


public class SpawnTeleportCancelListener implements Listener {

    private final SpawnCommand spawnCommand;

    public SpawnTeleportCancelListener(SpawnCommand spawnCommand) {
        this.spawnCommand = spawnCommand;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock()) {
            spawnCommand.cancelTeleport(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player) {
            spawnCommand.cancelTeleport((org.bukkit.entity.Player) event.getEntity());
        }
    }
}