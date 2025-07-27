package dev.malvaito.events;

import dev.malvaito.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final DatabaseManager databaseManager;
    
    public PlayerJoinListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Insertar o actualizar jugador en la tabla de stats
        String query = "INSERT INTO stats (player_uuid, player_nickname) VALUES (?, ?) " +
                       "ON CONFLICT(player_uuid) DO UPDATE SET player_nickname=excluded.player_nickname;";
        
        try (java.sql.PreparedStatement pstmt = databaseManager.getDatabaseConnection().prepareStatement(query)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
