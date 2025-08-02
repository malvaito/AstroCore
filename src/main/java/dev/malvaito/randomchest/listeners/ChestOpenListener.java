package dev.malvaito.randomchest.listeners;

import dev.malvaito.randomchest.ChestManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Malvaito
 */
public class ChestOpenListener implements Listener {

    private final ChestManager chestManager;
    private final Set<String> openedChests = new HashSet<>();

    public ChestOpenListener(ChestManager chestManager) {
        this.chestManager = chestManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getClickedBlock().getType() == Material.CHEST) {
            if (event.getClickedBlock().getState() instanceof Chest) {
                String chestLocationKey = chestManager.getLocationKey(event.getClickedBlock().getLocation());

                if (chestManager.isActiveChest(chestLocationKey)) {
                    
                    if (!openedChests.contains(chestLocationKey)) {
                        Location loc = event.getClickedBlock().getLocation();
                        String message = String.format("<green>¡%s abrió un cofre aleatorio en X:%.0f Y:%.0f Z:%.0f!</green>",
                                 event.getPlayer().getName(), loc.getX(), loc.getY(), loc.getZ());
                        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(message));
                        openedChests.add(chestLocationKey);
                    }
                }
            }
        }
    }
}