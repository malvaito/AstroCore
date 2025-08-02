package dev.malvaito.home;

import dev.malvaito.AstroCore;
import dev.malvaito.database.DatabaseManager;
import net.kyori.adventure.text.Component;
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

    private Component createMaxHomesReachedMessage(int maxHomes) {
        return miniMessage.deserialize("<red>Has alcanzado tu número máximo de hogares (</red><gold>" + maxHomes + "</gold><red>).</red>");
    }

    private Component createHomeSetMessage(String homeName) {
        return miniMessage.deserialize("<green>¡El hogar '</green><gold>" + homeName + "</gold><green>' ha sido establecido!</green>");
    }

    private Component createErrorSettingHomeMessage() {
        return miniMessage.deserialize("<red>Ocurrió un error al establecer tu hogar.</red>");
    }

    private Component createHomeTeleportedMessage(String homeName) {
        return miniMessage.deserialize("<green>¡Teletransportado al hogar '</green><gold>" + homeName + "</gold><green>'!</green>");
    }

    private Component createHomeNotFoundMessage(String homeName) {
        return miniMessage.deserialize("<red>Hogar '</red><gold>" + homeName + "</gold><red>' no encontrado. Usa /sethome <nombre> para establecer uno.</red>");
    }

    private Component createErrorTeleportingToHomeMessage() {
        return miniMessage.deserialize("<red>Ocurrió un error al teletransportarte a tu hogar.</red>");
    }

    private Component createListHomesMessage(String homesList) {
        return miniMessage.deserialize("<green>Tus hogares: </green><gold>" + homesList + "</gold>");
    }

    private Component createNoHomesSetMessage() {
        return miniMessage.deserialize("<red>No tienes ningún hogar establecido. Usa /sethome <nombre> para establecer uno.</red>");
    }

    private Component createErrorListingHomesMessage() {
        return miniMessage.deserialize("<red>Ocurrió un error al listar tus hogares.</red>");
    }

    private Component createHomeDeletedMessage(String homeName) {
        return miniMessage.deserialize("<green>¡El hogar '</green><gold>" + homeName + "</gold><green>' ha sido eliminado!</green>");
    }

    private Component createHomeNotFoundForDeletionMessage(String homeName) {
        return miniMessage.deserialize("<red>Hogar '</red><gold>" + homeName + "</gold><red>' no encontrado.</red>");
    }

    private Component createErrorDeletingHomeMessage() {
        return miniMessage.deserialize("<red>Ocurrió un error al eliminar tu hogar.</red>");
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
                            player.sendMessage(createMaxHomesReachedMessage(maxHomes));
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
                player.sendMessage(createHomeSetMessage(homeName));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting home: " + e.getMessage());
            player.sendMessage(createErrorSettingHomeMessage());
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
                        player.sendMessage(createHomeTeleportedMessage(homeName));
                    } else {
                        player.sendMessage(createHomeNotFoundMessage(homeName));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error teleporting to home: " + e.getMessage());
            player.sendMessage(createErrorTeleportingToHomeMessage());
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
                        player.sendMessage(createListHomesMessage(homesList.toString()));
                    } else {
                        player.sendMessage(createNoHomesSetMessage());
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error listing homes: " + e.getMessage());
            player.sendMessage(createErrorListingHomesMessage());
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
                    player.sendMessage(createHomeDeletedMessage(homeName));
                } else {
                    player.sendMessage(createHomeNotFoundForDeletionMessage(homeName));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error deleting home: " + e.getMessage());
            player.sendMessage(createErrorDeletingHomeMessage());
        }
    }
}