package dev.malvaito.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.UUID;

public class Feed implements CommandExecutor {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_SECONDS = 15;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Only players can use this command.</red>"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("astrocore.command.feed")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to use this command.</red>"));
            return true;
        }

        if (cooldowns.containsKey(playerUUID)) {
            long secondsLeft = ((cooldowns.get(playerUUID) / 1000) + COOLDOWN_SECONDS) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are on cooldown! Please wait <gold>" + secondsLeft + "</gold> seconds.</red>"));
                return true;
            }
        }

        cooldowns.put(playerUUID, System.currentTimeMillis());
        player.setSaturation(10);
        player.setFoodLevel(20);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been feed!</green>"));
        return true;
    }
}
