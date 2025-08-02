package dev.malvaito.commands;

import dev.malvaito.AstroCore;
import dev.malvaito.database.DatabaseManager;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Balance implements CommandExecutor {

    private final AstroCore plugin;
    private final DatabaseManager databaseManager;

    public Balance(AstroCore plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Solo los jugadores pueden usar este comando.</red>"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            double balance = getPlayerBalance(player.getUniqueId().toString());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Tu saldo: <green>" + balance + "</green> monedas.</gold>"));
        } else if (args.length == 1) {
            if (!player.hasPermission("astrocore.balance.other")) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You don't have permission to view other players' balances.</red>"));
                return true;
            }

            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found or has never played on the server.</red>"));
                return true;
            }

            double balance = getPlayerBalance(targetPlayer.getUniqueId().toString());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Balance of <yellow>" + targetPlayer.getName() + "</yellow>: <green>" + balance + "</green> coins.</gold>"));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /balance [player]</red>"));
        }
        return true;
    }

    private double getPlayerBalance(String uuid) {
        double balance = 0.0;
        try (Connection connection = databaseManager.getDatabaseConnection()) {
            String sql = "SELECT balance FROM economy WHERE player_uuid = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    balance = resultSet.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener el saldo del jugador: " + e.getMessage());
        }
        return balance;
    }
}
