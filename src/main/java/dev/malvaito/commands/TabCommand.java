package dev.malvaito.commands;

import dev.malvaito.AstroCore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TabCommand implements CommandExecutor {

    private final AstroCore plugin;
    private final MiniMessage miniMessage;

    public TabCommand(AstroCore plugin) {
        this.plugin = plugin;
        this.miniMessage = plugin.miniMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("astrocore.tab.reload")) {
                plugin.getTabManager().loadConfig();
                plugin.getTabManager().updateAllPlayersTab();
                sender.sendMessage(miniMessage.deserialize("<green>Tab configuration reloaded successfully!</green>"));
            } else {
                sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to use this command.</red>"));
            }
            return true;
        }
        sender.sendMessage(miniMessage.deserialize("<red>Usage: /tab reload</red>"));
        return true;
    }
}