package dev.malvaito.randomchest.commands;

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
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Usage: /randomchest <create|additem|spawn></gold>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Usage: /randomchest create <chest_name></gold>"));
                    return true;
                }
                String chestName = args[1];
                if (randomChest.getChestConfig().getConfig().contains("chests." + chestName)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Chest '</red><gold>" + chestName + "</gold><red>' already exists.</red>"));
                    return true;
                }
                randomChest.getChestConfig().getConfig().set("chests." + chestName + ".items", new java.util.ArrayList<String>());
                randomChest.getChestConfig().getConfig().set("chests." + chestName + ".random-slots", false);
                randomChest.getChestConfig().saveConfig();
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Chest '</green><gold>" + chestName + "</gold><green>' created in configuration.</green>"));
                break;
            case "spawn":
                if (!(sender instanceof org.bukkit.entity.Player)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>This command can only be executed by a player.</red>"));
                    return true;
                }
                player = (org.bukkit.entity.Player) sender;

                if (args.length < 3) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Usage: /randomchest spawn <chest_name> <here|random></gold>"));
                    return true;
                }

                String spawnChestName = args[1];
                String spawnType = args[2].toLowerCase();

                if (!randomChest.getChestConfig().getConfig().contains("chests." + spawnChestName)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Chest '</red><gold>" + spawnChestName + "</gold><red>' does not exist in configuration.</red>"));
                    return true;
                }

                if (spawnType.equals("here")) {
                    randomChest.getChestManager().createChestAtLocation(spawnChestName, player.getLocation());
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Chest of type '</green><gold>" + spawnChestName + "</gold><green>' spawned at your location.</green>"));
                } else if (spawnType.equals("random")) {
                    randomChest.generateRandomChest();
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Chest of type '</green><gold>" + spawnChestName + "</gold><green>' spawned at a random location.</green>"));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid spawn type. Usage: /randomchest spawn <chest_name> <here|random></red>"));
                }
                break;
            case "additem":
                if (!(sender instanceof org.bukkit.entity.Player)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>This command can only be executed by a player.</red>"));
                    return true;
                }
                player = (org.bukkit.entity.Player) sender;

                if (args.length < 3) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Usage: /randomchest additem <chest_name> <hand|gui></gold>"));
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
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must have an item in your hand to use 'hand'.</red>"));
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
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item added to chest '</green><gold>" + targetChestName + "</gold><green>' from your hand.</green>"));
                } else if (addType.equals("gui")) {
                    ChestAddItemGUI gui = new dev.malvaito.randomchest.gui.ChestAddItemGUI(plugin, randomChest, targetChestName);
                    gui.open(player);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Opening GUI for chest '</green><gold>" + targetChestName + "</gold><green>'.</green>"));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid add type. Usage: /randomchest additem <chest_name> <hand|gui></red>"));
                }
                break;
            default:
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Unknown command. Usage: /randomchest <create|additem|spawn></red>"));
                break;
        }

        return true;
    }
}