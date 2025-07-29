package dev.malvaito.randomchest;

import org.bukkit.Bukkit;
import dev.malvaito.randomchest.util.ItemSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
// Comentario para forzar la recompilación

public class ChestManager {

    private final JavaPlugin plugin;
    private final RandomChestConfig config;
    private final Random random;
    private final Map<String, Long> activeChests;

    public ChestManager(JavaPlugin plugin, RandomChestConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.random = new Random();
        this.activeChests = new HashMap<>();
        startChestCleanupTask();
    }

    public void createRandomChest(Location location) {
        // Obtener todos los tipos de cofres definidos en la configuración
        // Get all chest types defined in the configuration
        ConfigurationSection chestsSection = config.getConfig().getConfigurationSection("chests");
        if (chestsSection == null || chestsSection.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("No chest types defined in randomchest.yml");
            return;
        }

        List<String> chestTypes = new java.util.ArrayList<>(chestsSection.getKeys(false));
        String randomChestType = chestTypes.get(random.nextInt(chestTypes.size()));

        createChestAtLocation(randomChestType, location);
    }

    private Location getRandomLocation() {
        World world = Bukkit.getWorlds().get(0); // Obtener el primer mundo cargado
        if (world == null) return null;

        int spawnRadius = config.getConfig().getInt("spawn-radius", 100);
        int centerX = world.getSpawnLocation().getBlockX();
        int centerZ = world.getSpawnLocation().getBlockZ();

        for (int i = 0; i < 10; i++) { // Intentar 10 veces encontrar una ubicación válida
            int x = ThreadLocalRandom.current().nextInt(centerX - spawnRadius, centerX + spawnRadius + 1);
            int z = ThreadLocalRandom.current().nextInt(centerZ - spawnRadius, centerZ + spawnRadius + 1);
            int y = world.getHighestBlockYAt(x, z);

            Location loc = new Location(world, x, y + 1, z); // +1 para que el cofre no esté dentro del bloque
            if (loc.getBlock().getType() == Material.AIR && loc.clone().subtract(0,1,0).getBlock().getType().isSolid()) {
                return loc;
            }
        }
        return null;
    }

    private void fillChest(Chest chest, String chestTypeName) {
        ConfigurationSection chestSection = config.getConfig().getConfigurationSection("chests." + chestTypeName);
        if (chestSection == null) return;

        List<String> itemStrings = chestSection.getStringList("items");
        boolean randomSlots = chestSection.getBoolean("random-slots", false);

        if (itemStrings.isEmpty()) return;

        for (String itemString : itemStrings) {
            try {
                ItemStack item = ItemSerializer.itemStackFromBase64(itemString);

                if (randomSlots) {
                    int slot = random.nextInt(chest.getInventory().getSize());
                    chest.getInventory().setItem(slot, item);
                } else {
                    chest.getInventory().addItem(item);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Error deserializing item: " + e.getMessage());
                continue;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid item in configuration: " + itemString);
            }
        }
    }

    public void createChestAtLocation(String chestTypeName, Location location) {
        Block block = location.getBlock();
        block.setType(Material.CHEST);

        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            fillChest(chest, chestTypeName);
            activeChests.put(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ(), System.currentTimeMillis()); // Registrar el cofre activo
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>A chest of type '</green><gold>" + chestTypeName + "</gold><green>' has appeared at X: <gold>" + location.getBlockX() + "</gold>, Y: <gold>" + location.getBlockY() + "</gold>, Z: <gold>" + location.getBlockZ() + "</gold>!</green>"));
        } else {
            plugin.getLogger().warning("The block at the specified location is not a chest.");
        }
    }

    public boolean isActiveChest(String locationKey) {
        return activeChests.containsKey(locationKey);
    }

    public String getLocationKey(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public void removeChest(Location location) {
        Block block = location.getBlock();
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            chest.getInventory().clear();
            block.setType(Material.AIR);
            activeChests.remove(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            plugin.getLogger().info("Chest at " + location.toString() + " removed due to inactivity.");
        }
    }

    private void startChestCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                activeChests.entrySet().removeIf(entry -> {
                    String locString = entry.getKey();
                    long creationTime = entry.getValue();

                    String[] coords = locString.split(",");
                    if (coords.length != 3) {
                        plugin.getLogger().warning("Invalid location string in activeChests: " + locString);
                        return true;
                    }
                    Location loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));

                    Block block = loc.getBlock();
                    if (block.getState() instanceof Chest) {
                        Chest chest = (Chest) block.getState();
                        if (chest.getInventory().isEmpty()) {
                            if (currentTime - creationTime >= 10 * 60 * 1000) { // 10 minutos en milisegundos
                                removeChest(loc);
                                return true;
                            }
                        } else {
                            // Si el cofre tiene items, reiniciar el contador
                            activeChests.put(locString, currentTime);
                        }
                    } else {
                        // Si el bloque ya no es un cofre (ej. fue roto), eliminarlo del registro
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 20 * 60, 20 * 60); // Ejecutar cada minuto (20 ticks * 60 segundos)
    }
}