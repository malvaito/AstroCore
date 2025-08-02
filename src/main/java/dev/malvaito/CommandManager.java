package dev.malvaito;

import dev.malvaito.commands.Balance;
import dev.malvaito.commands.Economy;
import dev.malvaito.commands.Enderchest;
import dev.malvaito.commands.Feed;
import dev.malvaito.commands.Message;
import dev.malvaito.commands.Pay;
import dev.malvaito.commands.Reply;
import dev.malvaito.database.DatabaseManager;
import dev.malvaito.randomchest.RandomChest;
import dev.malvaito.randomchest.command.RandomChestCommand;
import dev.malvaito.spawn.SetSpawnCommand;
import dev.malvaito.spawn.SpawnCommand;
import dev.malvaito.tab.TabCommand;
import dev.malvaito.tpa.TPA;
import dev.malvaito.home.HomeCommand;
import dev.malvaito.home.HomeManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * @author Malvaito
 */
public class CommandManager {

    private final AstroCore plugin;
    private final DatabaseManager databaseManager;
    private final MiniMessage miniMessage;
    private final RandomChest randomChest;
    private final SpawnCommand spawnCommand;
    private final TPA tpa;
    private final HomeManager homeManager;

    public CommandManager(AstroCore plugin, DatabaseManager databaseManager, MiniMessage miniMessage, RandomChest randomChest, SpawnCommand spawnCommand, TPA tpa) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.miniMessage = miniMessage;
        this.randomChest = randomChest;
        this.spawnCommand = spawnCommand;
        this.tpa = tpa;
        this.homeManager = new HomeManager(plugin, databaseManager, miniMessage);
    }

    public void registerCommands() {
        plugin.getCommand("eco").setExecutor(new Economy(plugin, databaseManager));
        plugin.getCommand("balance").setExecutor(new Balance(plugin, databaseManager));
        plugin.getCommand("pay").setExecutor(new Pay(plugin, databaseManager));
        plugin.getCommand("msg").setExecutor(new Message(plugin, miniMessage));
        plugin.getCommand("r").setExecutor(new Reply(plugin));
        plugin.getCommand("tpa").setExecutor(tpa.getTpaCommand());
        plugin.getCommand("tpaccept").setExecutor(tpa.getTpaCommand());
        plugin.getCommand("tpahere").setExecutor(tpa.getTpaCommand());
        plugin.getCommand("tpadeny").setExecutor(tpa.getTpaCommand());
        plugin.getCommand("tab").setExecutor(new TabCommand(plugin));
        plugin.getCommand("setspawn").setExecutor(new SetSpawnCommand(plugin, miniMessage));
        plugin.getCommand("spawn").setExecutor(spawnCommand);
        plugin.getCommand("feed").setExecutor(new Feed());
        plugin.getCommand("enderchest").setExecutor(new Enderchest());
        plugin.getCommand("randomchest").setExecutor(new RandomChestCommand(plugin, randomChest));

        // Register Home commands
        plugin.getCommand("home").setExecutor(new HomeCommand(homeManager, miniMessage));
        plugin.getCommand("sethome").setExecutor(new HomeCommand(homeManager, miniMessage));
        plugin.getCommand("delhome").setExecutor(new HomeCommand(homeManager, miniMessage));
    }
}