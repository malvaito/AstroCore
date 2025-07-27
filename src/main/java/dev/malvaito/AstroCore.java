package dev.malvaito;

import org.bukkit.plugin.java.JavaPlugin;

import dev.malvaito.database.DatabaseManager;

public class AstroCore extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Lógica de inicio del plugin
        this.databaseManager = new DatabaseManager();
        this.databaseManager.establishConnection();
    }

    @Override
    public void onDisable() {
        // Lógica de apagado del plugin
        this.databaseManager.closeConnection();
    }
}