package dev.malvaito.randomchest.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import dev.malvaito.randomchest.util.ItemSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import dev.malvaito.randomchest.RandomChest;
import dev.malvaito.randomchest.gui.ChestAddItemGUI;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomChestCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final RandomChest randomChest;

    public RandomChestCommand(JavaPlugin plugin, RandomChest randomChest) {
        this.plugin = plugin;
        this.randomChest = randomChest;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        org.bukkit.entity.Player player = null;
        if (args.length == 0) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Uso: /randomchest <create|additem|spawn></gold>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Uso: /randomchest create <nombre_cofre></gold>"));
                    return true;
                }
                String chestName = args[1];
                if (randomChest.getChestConfig().getConfig().contains("chests." + chestName)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>El cofre '</red><gold>" + chestName + "</gold><red>' ya existe.</red>"));
                    return true;
                }
                randomChest.getChestConfig().getConfig().set("chests." + chestName + ".items", new java.util.ArrayList<String>());
                randomChest.getChestConfig().getConfig().set("chests." + chestName + ".random-slots", false);
                randomChest.getChestConfig().saveConfig();
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Cofre '</green><gold>" + chestName + "</gold><green>' creado en la configuración.</green>"));
                break;
            case "spawn":
                if (!(sender instanceof org.bukkit.entity.Player)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Este comando solo puede ser ejecutado por un jugador.</red>"));
                    return true;
                }
                player = (org.bukkit.entity.Player) sender;

                if (args.length < 3) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Uso: /randomchest spawn <nombre_cofre> <aqui|aleatorio></gold>"));
                    return true;
                }

                String spawnChestName = args[1];
                String spawnType = args[2].toLowerCase();

                if (!randomChest.getChestConfig().getConfig().contains("chests." + spawnChestName)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>El cofre '</red><gold>" + spawnChestName + "</gold><red>' no existe en la configuración.</red>"));
                    return true;
                }

                if (spawnType.equals("here")) {
                    randomChest.getChestManager().createChestAtLocation(spawnChestName, player.getLocation());
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Cofre de tipo '</green><gold>" + spawnChestName + "</gold><green>' generado en tu ubicación.</green>"));
                } else if (spawnType.equals("random")) {
                    randomChest.generateRandomChest();
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Cofre de tipo '</green><gold>" + spawnChestName + "</gold><green>' generado en una ubicación aleatoria.</green>"));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Tipo de aparición inválido. Uso: /randomchest spawn <nombre_cofre> <aqui|aleatorio></red>"));
                }
                break;
            case "additem":
                if (!(sender instanceof org.bukkit.entity.Player)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>This command can only be executed by a player.</red>"));
                    return true;
                }
                player = (org.bukkit.entity.Player) sender;

                if (args.length < 3) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Uso: /randomchest additem <nombre_cofre> <mano|gui></gold>"));
                    return true;
                }

                String targetChestName = args[1];
                String addType = args[2].toLowerCase();

                if (!randomChest.getChestConfig().getConfig().contains("chests." + targetChestName)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Chest '</red><gold>" + targetChestName + "</gold><red>' does not exist in configuration.</red>"));
                    return true;
                }

                if (addType.equals("hand")) {
                    org.bukkit.inventory.ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand.getType() == org.bukkit.Material.AIR) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes tener un objeto en la mano para usar 'mano'.</red>"));
                        return true;
                    }
                    java.util.List<String> items = randomChest.getChestConfig().getConfig().getStringList("chests." + targetChestName + ".items");
                    try {
                        items.add(ItemSerializer.itemStackToBase64(itemInHand));
                    } catch (IllegalStateException e) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Error serializing item: " + e.getMessage() + "</red>"));
                        return true;
                    }
                    randomChest.getChestConfig().getConfig().set("chests." + targetChestName + ".items", items);
                    randomChest.getChestConfig().saveConfig();
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Objeto añadido al cofre '</green><gold>" + targetChestName + "</gold><green>' desde tu mano.</green>"));
                } else if (addType.equals("gui")) {
                    ChestAddItemGUI gui = new dev.malvaito.randomchest.gui.ChestAddItemGUI(plugin, randomChest, targetChestName);
                    gui.open(player);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Abriendo GUI para el cofre '</green><gold>" + targetChestName + "</gold><green>'.</green>"));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Tipo de adición inválido. Uso: /randomchest additem <nombre_cofre> <mano|gui></red>"));
                }
                break;
            default:
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Comando desconocido. Uso: /randomchest <create|additem|spawn></red>"));
                break;
        }

        return true;
    }
}