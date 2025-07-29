package dev.malvaito.commands;

import dev.malvaito.database.DatabaseManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import dev.malvaito.AstroCore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Economy implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final MiniMessage miniMessage;

    public Economy(AstroCore plugin, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                handleGiveCommand(sender, args);
                break;
            case "set":
                handleSetCommand(sender, args);
                break;
            case "take":
                handleTakeCommand(sender, args);
                break;
            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<red>Usage: /eco <give|set|take> <player> <amount></red>"));
    }

    private void handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /eco give <player> <amount></red>"));
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid amount.</red>"));
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Amount must be positive.</red>"));
            return;
        }

        updatePlayerBalance(targetPlayer, amount, "give", sender);
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /eco set <player> <amount></red>"));
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid amount.</red>"));
            return;
        }

        if (amount < 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Amount cannot be negative.</red>"));
            return;
        }

        setPlayerBalance(targetPlayer, amount, sender);
    }

    private void handleTakeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /eco take <player> <amount></red>"));
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid amount.</red>"));
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Amount must be positive.</red>"));
            return;
        }

        updatePlayerBalance(targetPlayer, -amount, "take", sender);
    }

    private void updatePlayerBalance(OfflinePlayer player, double amount, String type, CommandSender sender) {
        Connection conn = databaseManager.getDatabaseConnection();
        if (conn == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Error: Database connection not available.</red>"));
            return;
        }
        try {
            String selectSql = "SELECT balance FROM economy WHERE player_uuid = ?;";
            String updateSql = "INSERT INTO economy (player_uuid, player_nickname, balance, total_spent, total_received, total_earned) VALUES (?, ?, ?, ?, ?, ?) " +
                               "ON CONFLICT(player_uuid) DO UPDATE SET balance = balance + ?, total_spent = total_spent + ?, total_received = total_received + ?, total_earned = total_earned + ?;";

            double currentBalance = 0.0;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    currentBalance = rs.getDouble("balance");
                }

            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, player.getUniqueId().toString());
                updateStmt.setString(2, player.getName());
                updateStmt.setDouble(3, amount); 
                updateStmt.setDouble(4, type.equals("take") ? amount : 0.0); 
                updateStmt.setDouble(5, type.equals("give") ? amount : 0.0); 
                updateStmt.setDouble(6, type.equals("give") ? amount : 0.0);

                updateStmt.setDouble(7, amount);
                updateStmt.setDouble(8, type.equals("take") ? amount : 0.0); 
                updateStmt.setDouble(9, type.equals("give") ? amount : 0.0); 
                updateStmt.setDouble(10, type.equals("give") ? amount : 0.0); 

                updateStmt.executeUpdate();

                double newBalance = currentBalance + amount;
                sender.sendMessage(miniMessage.deserialize("<green>Player <yellow>" + player.getName() + "</yellow>'s balance updated to <gold>" + String.format("%.2f", newBalance) + "</gold> coins.</green>"));
                if (player.isOnline()) {
                    player.getPlayer().sendMessage(miniMessage.deserialize("<green>Your balance has been updated. New balance: <gold>" + String.format("%.2f", newBalance) + "</gold> coins.</green>"));
                }
            }
        } catch (SQLException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Error updating player balance: " + e.getMessage() + "</red>"));
            e.printStackTrace();
        }
    }

    private void setPlayerBalance(OfflinePlayer player, double amount, CommandSender sender) {
        Connection conn = databaseManager.getDatabaseConnection();
        if (conn == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Error: Database connection not available.</red>"));
            return;
        }
        try {
            String updateSql = "INSERT INTO economy (player_uuid, player_nickname, balance, total_spent, total_received, total_earned) VALUES (?, ?, ?, ?, ?, ?) " +
                               "ON CONFLICT(player_uuid) DO UPDATE SET balance = ?;";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, player.getUniqueId().toString());
                updateStmt.setString(2, player.getName());
                updateStmt.setDouble(3, amount); 
                updateStmt.setDouble(4, 0.0); 
                updateStmt.setDouble(5, 0.0); 
                updateStmt.setDouble(6, 0.0);

                updateStmt.setDouble(7, amount);

                updateStmt.executeUpdate();

                sender.sendMessage(miniMessage.deserialize("<green>Player <yellow>" + player.getName() + "</yellow>'s balance set to <gold>" + String.format("%.2f", amount) + "</gold> coins.</green>"));
                if (player.isOnline()) {
                    player.getPlayer().sendMessage(miniMessage.deserialize("<green>Your balance has been set to: <gold>" + String.format("%.2f", amount) + "</gold> coins.</green>"));
                }
            }
        } catch (SQLException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Error setting player balance: " + e.getMessage() + "</red>"));
            e.printStackTrace();
        }
    }
}
