package dev.malvaito;

import dev.malvaito.database.DatabaseManager;
import dev.malvaito.listeners.PlayerJoinListener;
import dev.malvaito.listeners.PlayerQuitListener;
import dev.malvaito.listeners.TabPlayerJoinListener;
import dev.malvaito.randomchest.RandomChest;
import dev.malvaito.randomchest.listeners.ChestOpenListener;
import dev.malvaito.spawn.SpawnCommand;
import dev.malvaito.tpa.TPAManager;
import dev.malvaito.tpa.TPAListener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Listener;

import dev.malvaito.database.DatabaseManager;
import dev.malvaito.listeners.PlayerJoinListener;
import dev.malvaito.listeners.PlayerQuitListener;
import dev.malvaito.listeners.TabPlayerJoinListener;
import dev.malvaito.randomchest.RandomChest;
import dev.malvaito.randomchest.listeners.ChestOpenListener;
import dev.malvaito.spawn.SpawnCommand;

public class ListenerManager {

    private final AstroCore plugin;
    private final DatabaseManager databaseManager;
    private final MiniMessage miniMessage;
    private final RandomChest randomChest;
    private final SpawnCommand spawnCommand;
    private final TPAManager tpaManager;

    public ListenerManager(AstroCore plugin, DatabaseManager databaseManager, MiniMessage miniMessage, RandomChest randomChest, SpawnCommand spawnCommand, TPAManager tpaManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.miniMessage = miniMessage;
        this.randomChest = randomChest;
        this.spawnCommand = spawnCommand;
        this.tpaManager = tpaManager;
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, databaseManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TabPlayerJoinListener(plugin.getTabManager()), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChestOpenListener(randomChest.getChestManager()), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TPAListener(plugin, tpaManager), plugin);

        // Listener para cancelar teletransporte de spawn al moverse o recibir daño
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
                if (event.hasChangedBlock()) {
                    spawnCommand.cancelTeleport(event.getPlayer());
                }
            }

            public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
                if (event.getEntity() instanceof org.bukkit.entity.Player) {
                    spawnCommand.cancelTeleport((org.bukkit.entity.Player) event.getEntity());
                }
            }
        }, plugin);
    }
}