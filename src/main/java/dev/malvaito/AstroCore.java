package dev.malvaito;

import org.bukkit.plugin.java.JavaPlugin;
import dev.malvaito.database.DatabaseManager;
import dev.malvaito.listeners.PlayerJoinListener;
import dev.malvaito.listeners.PlayerQuitListener;
import dev.malvaito.commands.TPA;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AstroCore extends JavaPlugin {

    private DatabaseManager databaseManager;
    public MiniMessage miniMessage;

    @Override
    public void onEnable() {
        this.miniMessage = MiniMessage.miniMessage();
        // Lógica de inicio del plugin
        this.databaseManager = DatabaseManager.getInstance();

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, databaseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);


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


    }

    @Override
    public void onDisable() {
        // Lógica de apagado del plugin
        this.databaseManager.closeConnection();
    }
}