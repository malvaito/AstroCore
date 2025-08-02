package dev.malvaito.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;


public class Enderchest implements CommandExecutor {

    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Solo los jugadores pueden usar este comando.</red>"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("astrocore.command.enderchest")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando.</red>"));
            return true;
        }

        player.closeInventory();
        player.openInventory(player.getEnderChest());
        return true;
    }
}
