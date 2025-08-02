package dev.malvaito.commands;

import dev.malvaito.AstroCore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Reply implements CommandExecutor {

    private final MiniMessage miniMessage;
    private static final Map<UUID, UUID> lastMessaged = new HashMap<>();

    public Reply(AstroCore plugin) {
        this.miniMessage = MiniMessage.miniMessage();
    }

    public static void setLastMessaged(UUID sender, UUID receiver) {
        lastMessaged.put(receiver, sender);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Solo los jugadores pueden usar este comando.</red>"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Uso: /r <mensaje></red>"));
            return true;
        }

        UUID targetUUID = lastMessaged.get(player.getUniqueId());
        if (targetUUID == null) {
            player.sendMessage(miniMessage.deserialize("<red>No tienes a nadie a quien responder.</red>"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(miniMessage.deserialize("<red>El jugador al que intentas responder ya no está en línea.</red>"));
            lastMessaged.remove(player.getUniqueId());
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String message = messageBuilder.toString().trim();

        player.sendMessage(miniMessage.deserialize("<gray>[<white>Tú</white> -> <yellow>" + targetPlayer.getName() + "</yellow>] <reset>" + message + "</reset>"));
        targetPlayer.sendMessage(miniMessage.deserialize("<gray>[<yellow>" + player.getName() + "</yellow> -> <white>Tú</white>] <reset>" + message + "</reset>"));

        
        setLastMessaged(player.getUniqueId(), targetPlayer.getUniqueId());

        return true;
    }
}
