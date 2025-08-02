package dev.malvaito.listeners;

import dev.malvaito.AstroCore;
import dev.malvaito.database.DatabaseManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Malvaito
 */
public class PlayerJoinListener implements Listener {

    private final AstroCore plugin;
    private final DatabaseManager databaseManager;
    public PlayerJoinListener(AstroCore plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        Player player = event.getPlayer();
        

        insertOrUpdatePlayerStats(player);
        insertOrUpdatePlayerEconomy(player);
    }

    private void insertOrUpdatePlayerStats(Player player) {
        String query = "INSERT INTO stats (player_uuid, player_nickname) VALUES (?, ?) " +
                       "ON CONFLICT(player_uuid) DO UPDATE SET player_nickname=excluded.player_nickname;";
        try (java.sql.PreparedStatement pstmt = databaseManager.getDatabaseConnection().prepareStatement(query)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName());
            pstmt.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("Error inserting/updating player stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertOrUpdatePlayerEconomy(Player player) {
        String economyQuery = "INSERT INTO economy (player_uuid, player_nickname, balance, total_spent, total_received, total_earned) VALUES (?, ?, ?, ?, ?, ?) " +
                              "ON CONFLICT(player_uuid) DO UPDATE SET player_nickname=excluded.player_nickname;";
        try (java.sql.PreparedStatement pstmt = databaseManager.getDatabaseConnection().prepareStatement(economyQuery)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName());
            pstmt.setDouble(3, 0.0);
            pstmt.setDouble(4, 0.0);
            pstmt.setDouble(5, 0.0);
            pstmt.setDouble(6, 0.0);
            pstmt.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("Error inserting/updating player economy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
