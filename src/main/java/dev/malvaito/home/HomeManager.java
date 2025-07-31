package dev.malvaito.home;

import dev.malvaito.AstroCore;
import dev.malvaito.database.DatabaseManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeManager {

    private final AstroCore plugin;
    private final DatabaseManager databaseManager;
    private final MiniMessage miniMessage;

    public HomeManager(AstroCore plugin, DatabaseManager databaseManager, MiniMessage miniMessage) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.miniMessage = miniMessage;
        createHomeTable();
    }

    private void createHomeTable() {
        try (Connection connection = databaseManager.getDatabaseConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS homes (player_uuid VARCHAR(36), home_name VARCHAR(255), world VARCHAR(255), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, PRIMARY KEY (player_uuid, home_name))";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating homes table: " + e.getMessage());
        }
    }

    public void setHome(Player player, String homeName) {
        try (Connection connection = databaseManager.getDatabaseConnection()) {
            int maxHomes = 1;
            for (int i = 10; i >= 1; i--) {
                if (player.hasPermission("astrocore.home." + i)) {
                    maxHomes = i;
                    break;
                }
            }

            String countSql = "SELECT COUNT(*) FROM homes WHERE player_uuid = ?";
            try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
                countStatement.setString(1, player.getUniqueId().toString());
                try (ResultSet rs = countStatement.executeQuery()) {
                    if (rs.next()) {
                        int currentHomes = rs.getInt(1);
                        if (currentHomes >= maxHomes) {
                            player.sendMessage(miniMessage.deserialize("<red>You have reached your maximum number of homes (</red><gold>" + maxHomes + "</gold><red>).</red>"));
                            return;
                        }
                    }
                }
            }

            String sql = "INSERT OR REPLACE INTO homes (player_uuid, home_name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, homeName);
                statement.setString(3, player.getLocation().getWorld().getName());
                statement.setDouble(4, player.getLocation().getX());
                statement.setDouble(5, player.getLocation().getY());
                statement.setDouble(6, player.getLocation().getZ());
                statement.setFloat(7, player.getLocation().getYaw());
                statement.setFloat(8, player.getLocation().getPitch());
                statement.executeUpdate();
                player.sendMessage(miniMessage.deserialize("<green>Home '</green><gold>" + homeName + "</gold><green>' has been set!</green>"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting home: " + e.getMessage());
            player.sendMessage(miniMessage.deserialize("<red>An error occurred while setting your home.</red>"));
        }
    }

    public void teleportToHome(Player player, String homeName) {
        try (Connection connection = databaseManager.getDatabaseConnection()) {
            String sql = "SELECT world, x, y, z, yaw, pitch FROM homes WHERE player_uuid = ? AND home_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, homeName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String worldName = resultSet.getString("world");
                        double x = resultSet.getDouble("x");
                        double y = resultSet.getDouble("y");
                        double z = resultSet.getDouble("z");
                        float yaw = resultSet.getFloat("yaw");
                        float pitch = resultSet.getFloat("pitch");

                        player.teleport(new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch));
                        player.sendMessage(miniMessage.deserialize("<green>Teleported to home '</green><gold>" + homeName + "</gold><green>'!</green>"));
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>Home '</red><gold>" + homeName + "</gold><red>' not found. Use /sethome <name> to set one.</red>"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error teleporting to home: " + e.getMessage());
            player.sendMessage(miniMessage.deserialize("<red>An error occurred while teleporting to your home.</red>"));
        }
    }

    public void listHomes(Player player) {
        try (Connection connection = databaseManager.getDatabaseConnection()) {
            String sql = "SELECT home_name FROM homes WHERE player_uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    StringBuilder homesList = new StringBuilder();
                    int count = 0;
                    while (resultSet.next()) {
                        if (count > 0) {
                            homesList.append(", ");
                        }
                        homesList.append(resultSet.getString("home_name"));
                        count++;
                    }

                    if (count > 0) {
                        player.sendMessage(miniMessage.deserialize("<green>Your homes: </green><gold>" + homesList.toString() + "</gold>"));
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>You don't have any homes set. Use /sethome <name> to set one.</red>"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error listing homes: " + e.getMessage());
            player.sendMessage(miniMessage.deserialize("<red>An error occurred while listing your homes.</red>"));
        }
    }

    public void deleteHome(Player player, String homeName) {
        try (Connection connection = databaseManager.getDatabaseConnection()) {
            String sql = "DELETE FROM homes WHERE player_uuid = ? AND home_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, homeName);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    player.sendMessage(miniMessage.deserialize("<green>Home '</green><gold>" + homeName + "</gold><green>' has been deleted!</green>"));
                } else {
                    player.sendMessage(miniMessage.deserialize("<red>Home '</red><gold>" + homeName + "</gold><red>' not found.</red>"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error deleting home: " + e.getMessage());
            player.sendMessage(miniMessage.deserialize("<red>An error occurred while deleting your home.</red>"));
        }
    }
}