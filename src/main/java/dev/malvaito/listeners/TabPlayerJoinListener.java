package dev.malvaito.listeners;

import dev.malvaito.tab.TabManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabPlayerJoinListener implements Listener {

    private final TabManager tabManager;

    public TabPlayerJoinListener(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        tabManager.updatePlayerTab(event.getPlayer());
    }
}