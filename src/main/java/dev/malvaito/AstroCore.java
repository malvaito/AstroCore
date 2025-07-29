package dev.malvaito;

import org.bukkit.plugin.java.JavaPlugin;
import dev.malvaito.database.DatabaseManager;
import dev.malvaito.listeners.PlayerJoinListener;
import dev.malvaito.listeners.PlayerQuitListener;
import dev.malvaito.commands.TPA;
import dev.malvaito.commands.SetSpawnCommand;
import dev.malvaito.commands.SpawnCommand;
import dev.malvaito.tab.TabManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import dev.malvaito.randomchest.RandomChest;
import dev.malvaito.randomchest.listeners.ChestOpenListener;


public class AstroCore extends JavaPlugin {

    private DatabaseManager databaseManager;
    public MiniMessage miniMessage;
    private TabManager tabManager;
    private RandomChest randomChest;

    @Override
    public void onEnable() {
        // Inicializar RandomChest
        this.randomChest = new RandomChest(this);
        this.randomChest.onEnable();

        this.miniMessage = MiniMessage.miniMessage();
        // Lógica de inicio del plugin
        this.databaseManager = DatabaseManager.getInstance();

        // Cargar configuración
        saveDefaultConfig();

        // Inicializa el TabManager
        this.tabManager = new TabManager(this);
        // Carga la configuración del tab
        this.tabManager.loadConfig();
        // Actualiza el tab para todos los jugadores online
        this.tabManager.updateAllPlayersTab();

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, databaseManager), this);
        getServer().getPluginManager().registerEvents(new dev.malvaito.listeners.TabPlayerJoinListener(this.tabManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new ChestOpenListener(this.randomChest.getChestManager()), this);

        SpawnCommand spawnCommand = new SpawnCommand(this, miniMessage);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerMove(PlayerMoveEvent event) {
                if (event.hasChangedBlock()) {
                    spawnCommand.cancelTeleport(event.getPlayer());
                }
            }

            @EventHandler
            public void onEntityDamage(EntityDamageEvent event) {
                if (event.getEntity() instanceof Player) {
                    spawnCommand.cancelTeleport((Player) event.getEntity());
                }
            }
        }, this);

        // Registrar comandos
        getCommand("eco").setExecutor(new dev.malvaito.commands.Economy(this, databaseManager));
        getCommand("balance").setExecutor(new dev.malvaito.commands.Balance(this, databaseManager));
        getCommand("pay").setExecutor(new dev.malvaito.commands.Pay(this, databaseManager));
        getCommand("msg").setExecutor(new dev.malvaito.commands.Message(this, miniMessage));
        TPA tpaCommand = new dev.malvaito.commands.TPA(this, databaseManager);
        getServer().getPluginManager().registerEvents(tpaCommand, this);
        getCommand("r").setExecutor(new dev.malvaito.commands.Reply(this));
        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpaccept").setExecutor(tpaCommand);
        getCommand("tpahere").setExecutor(tpaCommand);
        getCommand("tpadeny").setExecutor(tpaCommand);
        getCommand("tab").setExecutor(new dev.malvaito.commands.TabCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this, miniMessage));
        getCommand("spawn").setExecutor(spawnCommand);

        // Registrar comandos de RandomChest
        getCommand("randomchest").setExecutor(new dev.malvaito.randomchest.commands.RandomChestCommand(this, this.randomChest));
        getCommand("randomchest").setTabCompleter(new dev.malvaito.randomchest.commands.RandomChestTabCompleter(this.randomChest));



    }

    @Override
    public void onDisable() {
        // Lógica de apagado del plugin
        this.databaseManager.closeConnection();
        // Deshabilitar RandomChest
        this.randomChest.onDisable();
    }

    public TabManager getTabManager() {
        return tabManager;
    }
}