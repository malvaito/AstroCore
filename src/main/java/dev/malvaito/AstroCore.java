package dev.malvaito;

import org.bukkit.plugin.java.JavaPlugin;

import dev.malvaito.database.DatabaseManager;
import dev.malvaito.events.PlayerJoinListener;

public class AstroCore extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Lógica de inicio del plugin
        this.databaseManager = DatabaseManager.getInstance();

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, databaseManager), this);

        // Registrar comandos
        getCommand("eco").setExecutor(new dev.malvaito.commands.Economy(this, databaseManager));
        getCommand("balance").setExecutor(new dev.malvaito.commands.Balance(this, databaseManager));
        getCommand("pay").setExecutor(new dev.malvaito.commands.Pay(this, databaseManager));
        getCommand("msg").setExecutor(new dev.malvaito.commands.Message(this));
        getCommand("r").setExecutor(new dev.malvaito.commands.Reply(this));
    }

    @Override
    public void onDisable() {
        // Lógica de apagado del plugin
        this.databaseManager.closeConnection();
    }
}