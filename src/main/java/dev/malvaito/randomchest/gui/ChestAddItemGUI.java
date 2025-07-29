package dev.malvaito.randomchest.gui;

import dev.malvaito.randomchest.RandomChest;
import dev.malvaito.randomchest.util.ItemSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChestAddItemGUI implements Listener {

    private final JavaPlugin plugin;
    private final RandomChest randomChest;
    private final String chestName;
    private final Inventory inventory;

    public ChestAddItemGUI(JavaPlugin plugin, RandomChest randomChest, String chestName) {
        this.plugin = plugin;
        this.randomChest = randomChest;
        this.chestName = chestName;
        this.inventory = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<gold>Add Items to </gold><green>" + chestName + "</green>"));

        // Cargar ítems existentes en la GUI
        List<String> existingItems = randomChest.getChestConfig().getConfig().getStringList("chests." + chestName + ".items");
        for (String itemString : existingItems) {
            try {
                inventory.addItem(ItemSerializer.itemStackFromBase64(itemString));
            } catch (IOException e) {
                plugin.getLogger().warning("Error deserializing item for GUI: " + e.getMessage());
            }
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            // Desregistrar el listener para evitar múltiples llamadas
            InventoryCloseEvent.getHandlerList().unregister(this);

            List<String> newItems = new ArrayList<>();
            for (ItemStack item : inventory.getContents()) {
                if (item != null) {
                    try {
                        newItems.add(ItemSerializer.itemStackToBase64(item));
                    } catch (IllegalStateException e) {
                        plugin.getLogger().warning("Error serializing item from GUI: " + e.getMessage());
                    }
                }
            }
            randomChest.getChestConfig().getConfig().set("chests." + chestName + ".items", newItems);
            randomChest.getChestConfig().saveConfig();
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Items saved to chest '</green><gold>" + chestName + "</gold><green>'.</green>"));
        }
    }
}