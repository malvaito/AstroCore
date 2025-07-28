package dev.malvaito.tab;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class TabConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public TabConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        configFile = new File(plugin.getDataFolder(), "tab.yml");
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource("tab.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    plugin.getLogger().warning("tab.yml not found in plugin resources. Creating empty file.");
                    configFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create tab.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getHeader() {
        return config.getString("tab.header", "<green>Welcome to the server!</green>");
    }

    public String getFooter() {
        return config.getString("tab.footer", "<yellow>Enjoy your stay!</yellow>");
    }

    public String getTabFormat() {
        return config.getString("tab.tab-format", "<gray>%player_name%</gray>");
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save tab.yml: " + e.getMessage());
        }
    }
}