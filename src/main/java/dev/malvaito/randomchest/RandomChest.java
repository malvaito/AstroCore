package dev.malvaito.randomchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class RandomChest {

    private final JavaPlugin plugin;


    private static RandomChest instance;
    private RandomChestConfig config;
    private ChestManager chestManager;


    public RandomChest(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        this.config = new RandomChestConfig(plugin);
        this.chestManager = new ChestManager(plugin, this.config);
    }

    public void onEnable() {
        plugin.getLogger().info("RandomChest functionality enabled!");
    }

    public void onDisable() {
        plugin.getLogger().info("RandomChest functionality disabled!");
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public static RandomChest getInstance() {
        return instance;
    }

    public RandomChestConfig getChestConfig() {
        return config;
    }

    public void generateRandomChest() {
        org.bukkit.World world = Bukkit.getWorlds().get(0);
        if (world == null) {
            plugin.getLogger().warning("No se encontró ningún mundo para generar el cofre.");
            return;
        }

        int spawnRadius = config.getSpawnRadius();
        Random random = new Random();

        
        
        int x = random.nextInt(2 * spawnRadius) - spawnRadius;
        int z = random.nextInt(2 * spawnRadius) - spawnRadius;
        int y = world.getHighestBlockYAt(x, z);

        Location spawnLocation = new Location(world, x, y, z);
        chestManager.createRandomChest(spawnLocation);
    }
}
