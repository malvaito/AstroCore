package dev.malvaito.tab;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TabManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage;
    private TabConfig tabConfig;

    public TabManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadConfig();
    }

    public void loadConfig() {
        this.tabConfig = new TabConfig(plugin);
        this.tabConfig.load();
    }

    public void updatePlayerTab(Player player) {
        // Actualiza el encabezado y pie de página del tab para un jugador
        Component header = miniMessage.deserialize(tabConfig.getHeader());
        Component footer = miniMessage.deserialize(tabConfig.getFooter());

        player.sendPlayerListHeader(header);
        player.sendPlayerListFooter(footer);

        // Actualiza el formato del nombre del jugador en la lista del tab
        String formattedName = tabConfig.getTabFormat();
        formattedName = PlaceholderAPI.setPlaceholders(player, formattedName);
        player.playerListName(miniMessage.deserialize(formattedName));
    }

    public void updateAllPlayersTab() {
        // Actualiza el encabezado y pie de página del tab para todos los jugadores
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayerTab(player);
        }
    }

    public TabConfig getTabConfig() {
        return tabConfig;
    }
}