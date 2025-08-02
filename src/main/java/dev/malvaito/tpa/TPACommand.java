package dev.malvaito.tpa;

import dev.malvaito.AstroCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TPACommand implements CommandExecutor {

    private final AstroCore plugin;
    private final TPAManager tpaManager;

    public TPACommand(AstroCore plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.miniMessage.deserialize("<red>Solo los jugadores pueden usar este comando.</red>"));
            return true;
        }

        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();

        if (tpaManager.isOnCooldown(player.getUniqueId())) {
            long remainingTime = tpaManager.getRemainingCooldown(player.getUniqueId());
            player.sendMessage(plugin.miniMessage.deserialize("<red>Estás en enfriamiento. Por favor, espera " + remainingTime + " segundos.</red>"));
            return true;
        }

        switch (commandName) {
            case "tpa":
                handleTpaCommand(player, args);
                break;
            case "tpaccept":
                handleTpAcceptCommand(player);
                break;
            case "tpadeny":
                handleTpDenyCommand(player);
                break;
            case "tpahere":
                handleTpaHereCommand(player, args);
                break;
            default:
                return false;
        }
        return true;
    }

    private void handleTpaCommand(Player player, String[] args) {
        if (args.length == 1) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.miniMessage.deserialize("<red>No puedes enviarte una solicitud de teletransporte a ti mismo.</red>"));
                    return;
                }
                tpaManager.getTpaRequests().put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(plugin.miniMessage.deserialize("<green>Has enviado una solicitud de teletransporte a <gold>" + target.getName() + "</gold>.</green>"));
                target.sendMessage(plugin.miniMessage.deserialize("<gold>" + player.getName() + "</gold> te ha enviado una solicitud de teletransporte. Usa <green>/tpaccept</green> para aceptar o <red>/tpadeny</red> para denegar."));
                tpaManager.addCooldown(player.getUniqueId(), 60 * 1000);
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>Jugador no encontrado o no está en línea.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>Uso: /tpa <jugador></red>"));
        }
    }

    private void handleTpAcceptCommand(Player player) {
        if (tpaManager.getTpaRequests().containsKey(player.getUniqueId())) {
            UUID requesterUUID = tpaManager.getTpaRequests().remove(player.getUniqueId());
            Player requester = plugin.getServer().getPlayer(requesterUUID);
            if (requester != null && requester.isOnline()) {
                player.sendMessage(plugin.miniMessage.deserialize("<green>Has aceptado la solicitud de teletransporte de <gold>" + requester.getName() + "</gold>.</green>"));
                requester.sendMessage(plugin.miniMessage.deserialize("<green>Tu solicitud de teletransporte ha sido aceptada por <gold>" + player.getName() + "</gold>. Teletransportando en 5 segundos...</green>"));

                tpaManager.addTeleportTask(requester.getUniqueId(), plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (requester.isOnline() && tpaManager.getPlayerLocations().containsKey(requester.getUniqueId()) && tpaManager.getPlayerLocations().get(requester.getUniqueId()).equals(requester.getLocation())) {
                        requester.teleport(player.getLocation());
                        requester.sendMessage(plugin.miniMessage.deserialize("<green>Has sido teletransportado a <gold>" + player.getName() + "</gold>.</green>"));
                    } else {
                        requester.sendMessage(plugin.miniMessage.deserialize("<red>Teletransporte cancelado porque te moviste o recibiste daño.</red>"));
                    }
                    tpaManager.getTeleportTasks().remove(requester.getUniqueId());
                    tpaManager.getPlayerLocations().remove(requester.getUniqueId());
                }, 20L * 5), requester.getLocation());

                tpaManager.addCooldown(player.getUniqueId(), 5 * 1000);
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>El jugador que envió la solicitud ya no está en línea.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>No tienes solicitudes de teletransporte pendientes.</red>"));
        }
    }

    private void handleTpDenyCommand(Player player) {
        if (tpaManager.getTpaRequests().containsKey(player.getUniqueId())) {
            UUID requesterUUID = tpaManager.getTpaRequests().remove(player.getUniqueId());
            Player requester = plugin.getServer().getPlayer(requesterUUID);
            if (requester != null && requester.isOnline()) {
                player.sendMessage(plugin.miniMessage.deserialize("<red>Has denegado la solicitud de teletransporte de <gold>" + requester.getName() + "</gold>.</red>"));
                requester.sendMessage(plugin.miniMessage.deserialize("<red>Tu solicitud de teletransporte ha sido denegada por <gold>" + player.getName() + "</gold>.</red>"));
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>The player who sent the request is no longer online.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>You have no pending teleport requests.</red>"));
        }
    }

    private void handleTpaHereCommand(Player player, String[] args) {
        if (args.length == 1) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.miniMessage.deserialize("<red>No puedes enviarte una solicitud de teletransporte a ti mismo.</red>"));
                    return;
                }
                tpaManager.getTpaRequests().put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(plugin.miniMessage.deserialize("<green>Has enviado una solicitud para que <gold>" + target.getName() + "</gold> se teletransporte a ti.</green>"));
                target.sendMessage(plugin.miniMessage.deserialize("<gold>" + player.getName() + "</gold> te ha enviado una solicitud para teletransportarse a ti. Usa <green>/tpaccept</green> para aceptar o <red>/tpadeny</red> para denegar.</green>"));
                tpaManager.addCooldown(player.getUniqueId(), 60 * 1000);
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>Jugador no encontrado o no está en línea.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>Uso: /tpahere <jugador></red>"));
        }
    }
}