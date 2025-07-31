package dev.malvaito;

import org.bukkit.plugin.java.JavaPlugin;
import dev.malvaito.database.DatabaseManager;
import dev.malvaito.tab.TabManager;
import dev.malvaito.tpa.TPA;
import net.kyori.adventure.text.minimessage.MiniMessage;
import dev.malvaito.randomchest.RandomChest;
import dev.malvaito.randomchest.scheduler.RandomChestScheduler;
import dev.malvaito.spawn.SpawnCommand;


public class AstroCore extends JavaPlugin {

    private DatabaseManager databaseManager;
    public MiniMessage miniMessage;
    private TabManager tabManager;
    private RandomChest randomChest;

    @Override
    public void onEnable() {

        this.randomChest = new RandomChest(this);
        this.randomChest.onEnable();


        new RandomChestScheduler(this.randomChest).runTaskTimer(this, 0L, 90000L);

        this.miniMessage = MiniMessage.miniMessage();

        this.databaseManager = DatabaseManager.getInstance();


        saveDefaultConfig();


        this.tabManager = new TabManager(this);

        this.tabManager.loadConfig();

        this.tabManager.updateAllPlayersTab();

        // Inicializar y registrar comandos
        SpawnCommand spawnCommand = new SpawnCommand(this, miniMessage);
        TPA tpa = new TPA(this);

        CommandManager commandManager = new CommandManager(this, databaseManager, miniMessage, randomChest, spawnCommand, tpa);
        commandManager.registerCommands();

        // Inicializar y registrar listeners
        ListenerManager listenerManager = new ListenerManager(this, databaseManager, miniMessage, randomChest, spawnCommand, tpa.getTpaManager());
        listenerManager.registerListeners();




    }

    @Override
    public void onDisable() {

        this.databaseManager.closeConnection();

        this.randomChest.onDisable();
    }

    public TabManager getTabManager() {
        return tabManager;
    }
}