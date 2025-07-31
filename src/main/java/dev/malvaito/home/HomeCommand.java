package dev.malvaito.home;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final HomeManager homeManager;
    private final MiniMessage miniMessage;

    public HomeCommand(HomeManager homeManager, MiniMessage miniMessage) {
        this.homeManager = homeManager;
        this.miniMessage = miniMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only players can use this command.</red>"));
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length == 0) {
                homeManager.listHomes(player);
                return true;
            } else {
                String homeName = args[0].toLowerCase();
                homeManager.teleportToHome(player, homeName);
                return true;
            }
        } else if (args.length == 0) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /" + command.getName() + " <home_name></red>"));
            return true;
        }

        String homeName = args[0].toLowerCase();

        if (command.getName().equalsIgnoreCase("sethome")) {
            homeManager.setHome(player, homeName);
        } else if (command.getName().equalsIgnoreCase("delhome")) {
            homeManager.deleteHome(player, homeName);
        }

        return true;
    }


}