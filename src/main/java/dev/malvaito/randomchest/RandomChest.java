package dev.malvaito.randomchest;

import org.bukkit.plugin.java.JavaPlugin;

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
}