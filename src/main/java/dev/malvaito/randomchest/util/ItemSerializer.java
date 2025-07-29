package dev.malvaito.randomchest.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)

            dataOutput.writeObject(itemStack);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
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
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack itemStack = (ItemStack) dataInput.readObject();

            dataInput.close();
            return itemStack;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}