package dev.malvaito.commands;

import dev.malvaito.AstroCore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.malvaito.commands.Reply.setLastMessaged;

public class Message implements CommandExecutor {

    private final MiniMessage miniMessage;

    public Message(AstroCore plugin) {
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only players can use this command.</red>"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /msg <player> <message></red>"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(miniMessage.deserialize("<red>Player not found or is not online.</red>"));
            return true;
        }

        if (player.equals(targetPlayer)) {
            player.sendMessage(miniMessage.deserialize("<red>You cannot message yourself.</red>"));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        player.sendMessage(miniMessage.deserialize("<gray>[<white>You</white> -> <yellow>" + targetPlayer.getName() + "</yellow>] <reset>" + message + "</reset>"));
        targetPlayer.sendMessage(miniMessage.deserialize("<gray>[<yellow>" + player.getName() + "</yellow> -> <white>You</white>] <reset>" + message + "</reset>"));

        setLastMessaged(player.getUniqueId(), targetPlayer.getUniqueId());

        return true;
    }
}
