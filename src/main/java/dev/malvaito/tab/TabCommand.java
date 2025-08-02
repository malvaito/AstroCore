package dev.malvaito.tab;

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
                sender.sendMessage(miniMessage.deserialize("<green>¡Configuración de la pestaña recargada exitosamente!</green>"));
            } else {
                sender.sendMessage(miniMessage.deserialize("<red>No tienes permiso para usar este comando.</red>"));
            }
            return true;
        }
        sender.sendMessage(miniMessage.deserialize("<red>Uso: /tab reload</red>"));
        return true;
    }
}