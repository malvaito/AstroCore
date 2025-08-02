package dev.malvaito.tpa;

import dev.malvaito.AstroCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

/**
 * @author Malvaito
 */
public class TPAListener implements Listener {

    private final AstroCore plugin;
    private final TPAManager tpaManager;

    public TPAListener(AstroCore plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (tpaManager.getTeleportTasks().containsKey(playerId)) {
            Location from = tpaManager.getPlayerLocations().get(playerId);
            Location to = event.getTo();
            if (from != null && to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())) {
                tpaManager.cancelTeleport(playerId);
                event.getPlayer().sendMessage(plugin.miniMessage.deserialize("<red>Teletransporte cancelado: ¡Te moviste!</red>"));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();
            if (tpaManager.getTeleportTasks().containsKey(playerId)) {
                tpaManager.cancelTeleport(playerId);
                player.sendMessage(plugin.miniMessage.deserialize("<red>Teletransporte cancelado: ¡Recibiste daño!</red>"));
            }
        }
    }
}