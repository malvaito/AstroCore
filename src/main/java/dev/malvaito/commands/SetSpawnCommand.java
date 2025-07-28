package dev.malvaito.commands;

import dev.malvaito.AstroCore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

    private final AstroCore plugin;
    private final MiniMessage miniMessage;

    public SetSpawnCommand(AstroCore plugin, MiniMessage miniMessage) {
        this.plugin = plugin;
        this.miniMessage = miniMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Solo los jugadores pueden usar este comando.</red>"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("astrocore.command.setspawn")) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to use this command.</red>"));
            return true;
        }

        plugin.getConfig().set("spawn.world", player.getWorld().getName());
        plugin.getConfig().set("spawn.x", player.getLocation().getX());
        plugin.getConfig().set("spawn.y", player.getLocation().getY());
        plugin.getConfig().set("spawn.z", player.getLocation().getZ());
        plugin.getConfig().set("spawn.yaw", player.getLocation().getYaw());
        plugin.getConfig().set("spawn.pitch", player.getLocation().getPitch());
        plugin.saveConfig();

        player.sendMessage(miniMessage.deserialize("<green>Spawn location set to your current location!</green>"));
        return true;
    }
}