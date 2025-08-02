package dev.malvaito.commands;

import dev.malvaito.AstroCore;
import dev.malvaito.database.DatabaseManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Pay implements CommandExecutor {

    private final AstroCore plugin;
    private final DatabaseManager databaseManager;
    private final MiniMessage miniMessage;

    public Pay(AstroCore plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Solo los jugadores pueden usar este comando.</red>"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Uso: /pay <jugador> <cantidad></red>"));
            return true;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            player.sendMessage(miniMessage.deserialize("<red>Jugador no encontrado.</red>"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(miniMessage.deserialize("<red>Cantidad inválida.</red>"));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(miniMessage.deserialize("<red>La cantidad debe ser positiva.</red>"));
            return true;
        }

        if (player.getUniqueId().equals(targetPlayer.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>No puedes pagarte a ti mismo.</red>"));
            return true;
        }

        double playerBalance = getPlayerBalance(player.getUniqueId().toString());
        if (playerBalance < amount) {
            player.sendMessage(miniMessage.deserialize("<red>No tienes suficiente dinero.</red>"));
            return true;
        }

        
        updatePlayerBalance(player, -amount, "pay_sent");
        updatePlayerBalance(targetPlayer, amount, "pay_received");

        player.sendMessage(miniMessage.deserialize("<green>Pagaste <yellow>" + targetPlayer.getName() + "</yellow> <gold>" + String.format("%.2f", amount) + "</gold> monedas.</green>"));
        if (targetPlayer.isOnline()) {
            targetPlayer.getPlayer().sendMessage(miniMessage.deserialize("<green>Recibiste <gold>" + String.format("%.2f", amount) + "</gold> monedas de <yellow>" + player.getName() + ".</yellow></green>"));
        }

        return true;
    }

    private double getPlayerBalance(String uuid) {
        double balance = 0.0;
        try (Connection connection = databaseManager.getDatabaseConnection()) {
            String sql = "SELECT balance FROM economy WHERE player_uuid = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    balance = rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting player balance: " + e.getMessage());
        }
        return balance;
    }

    private void updatePlayerBalance(OfflinePlayer player, double amount, String type) {
        Connection conn = databaseManager.getDatabaseConnection();
        if (conn == null) {
            plugin.getLogger().severe("Database connection not available for updating balance.");
            return;
        }
        try {
            String selectSql = "SELECT balance FROM economy WHERE player_uuid = ?;";
            String updateSql = "INSERT INTO economy (player_uuid, player_nickname, balance, total_spent, total_received, total_earned) VALUES (?, ?, ?, ?, ?, ?) " +
                               "ON CONFLICT(player_uuid) DO UPDATE SET balance = balance + ?, total_spent = total_spent + ?, total_received = total_received + ?, total_earned = total_earned + ?;";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, player.getUniqueId().toString());
                updateStmt.setString(2, player.getName());
                updateStmt.setDouble(3, amount); 
                updateStmt.setDouble(4, type.equals("pay_sent") ? amount : 0.0);
                updateStmt.setDouble(5, type.equals("pay_received") ? amount : 0.0); 
                updateStmt.setDouble(6, type.equals("pay_received") ? amount : 0.0); 

                updateStmt.setDouble(7, amount); 
                updateStmt.setDouble(8, type.equals("pay_sent") ? amount : 0.0); 
                updateStmt.setDouble(9, type.equals("pay_received") ? amount : 0.0);
                updateStmt.setDouble(10, type.equals("pay_received") ? amount : 0.0); 

                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating player balance for pay command: " + e.getMessage());
        }
    }
}
