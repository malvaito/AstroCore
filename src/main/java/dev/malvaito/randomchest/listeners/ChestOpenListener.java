package dev.malvaito.randomchest.listeners;

import dev.malvaito.randomchest.ChestManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.Set;

public class ChestOpenListener implements Listener {

    private final ChestManager chestManager;
    private final Set<String> openedChests = new HashSet<>();

    public ChestOpenListener(ChestManager chestManager) {
        this.chestManager = chestManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        // Solo procesar si es un click derecho en un bloque
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getClickedBlock().getType() == Material.CHEST) {
            if (event.getClickedBlock().getState() instanceof Chest) {
                String chestLocationKey = chestManager.getLocationKey(event.getClickedBlock().getLocation());

                if (chestManager.isActiveChest(chestLocationKey)) {
                    // Si el cofre no ha enviado el mensaje antes
                    if (!openedChests.contains(chestLocationKey)) {
                        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You have opened a random chest!</green>"));
                        openedChests.add(chestLocationKey); // Marcar como que ya envió el mensaje
                    }
                }
            }
        }
    }
}