package dev.malvaito.randomchest.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.IOException;

public class ItemSerializer {

    /**
     * Serializa un ItemStack a una cadena Base64.
     *
     * @param itemStack El ItemStack a serializar.
     * @return La cadena Base64 que representa el ItemStack.
     * @throws IllegalStateException Si ocurre un error de E/S durante la serialización.
     */
    public static String itemStackToBase64(ItemStack itemStack) throws IllegalStateException {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("item", itemStack);
            return Base64Coder.encodeLines(config.saveToString().getBytes());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * Deserializa una cadena Base64 a un ItemStack.
     *
     * @param data La cadena Base64 a deserializar.
     * @return El ItemStack deserializado.
     * @throws IOException Si ocurre un error de E/S durante la deserialización.
     */
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