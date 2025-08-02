package dev.malvaito.spawn;

import dev.malvaito.AstroCore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor {

    private final AstroCore plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, Long> teleportingPlayers;
    private final Map<UUID, Location> playerLocations;

    public SpawnCommand(AstroCore plugin, MiniMessage miniMessage) {
        this.plugin = plugin;
        this.miniMessage = miniMessage;
        this.teleportingPlayers = new HashMap<>();
        this.playerLocations = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Solo los jugadores pueden usar este comando.</red>"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (teleportingPlayers.containsKey(playerUUID)) {
            player.sendMessage(miniMessage.deserialize("<red>Ya estás siendo teletransportado.</red>"));
            return true;
        }

        if (!plugin.getConfig().contains("spawn.world")) {
            player.sendMessage(miniMessage.deserialize("<red>Ubicación de spawn no establecida. Usa /setspawn para establecerla.</red>"));
            return true;
        }

        double x = plugin.getConfig().getDouble("spawn.x");
        double y = plugin.getConfig().getDouble("spawn.y");
        double z = plugin.getConfig().getDouble("spawn.z");
        float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");
        String worldName = plugin.getConfig().getString("spawn.world");

        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

        player.sendMessage(miniMessage.deserialize("<yellow>Teletransportando al spawn en 5 segundos. No te muevas ni recibas daño.</yellow>"));
        teleportingPlayers.put(playerUUID, System.currentTimeMillis());
        playerLocations.put(playerUUID, player.getLocation());

        new BukkitRunnable() {
            int countdown = 5;
            @Override
            public void run() {
                if (!teleportingPlayers.containsKey(playerUUID)) {
                    cancel();
                    return;
                }

                if (player.getLocation().distanceSquared(playerLocations.get(playerUUID)) > 0.1 || player.getNoDamageTicks() > 0) {
                    player.sendMessage(miniMessage.deserialize("<red>Teletransporte cancelado: te moviste o recibiste daño.</red>"));
                    teleportingPlayers.remove(playerUUID);
                    playerLocations.remove(playerUUID);
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    player.teleport(spawnLocation);
                    player.sendMessage(miniMessage.deserialize("<green>¡Has sido teletransportado al spawn!</green>"));
                    teleportingPlayers.remove(playerUUID);
                    playerLocations.remove(playerUUID);
                    cancel();
                } else {
                    player.sendMessage(miniMessage.deserialize("<yellow>Teletransportando en: " + countdown + "...</yellow>"));
                    countdown--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    public void cancelTeleport(Player player) {
        teleportingPlayers.remove(player.getUniqueId());
        playerLocations.remove(player.getUniqueId());
    }
}