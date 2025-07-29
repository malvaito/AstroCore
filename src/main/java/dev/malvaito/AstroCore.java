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
import dev.malvaito.randomchest.scheduler.RandomChestScheduler;


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
        getCommand("feed").setExecutor(new dev.malvaito.commands.Feed());


        getCommand("randomchest").setExecutor(new dev.malvaito.commands.RandomChestCommand(this, this.randomChest));




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