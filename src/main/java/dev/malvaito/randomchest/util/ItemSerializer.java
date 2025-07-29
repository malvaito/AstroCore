package dev.malvaito.randomchest.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.IOException;

public class ItemSerializer {


    public static String itemStackToBase64(ItemStack itemStack) throws IllegalStateException {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("item", itemStack);
            return Base64Coder.encodeLines(config.saveToString().getBytes());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }


    public static ItemStack itemStackFromBase64(String data) throws IOException {
        try {
            String yamlString = new String(Base64Coder.decodeLines(data));
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yamlString);
            return config.getItemStack("item");
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            throw new IOException("Unable to load item stack from configuration.", e);
        }
    }
}