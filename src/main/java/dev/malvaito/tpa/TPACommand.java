package dev.malvaito.tpa;

import dev.malvaito.AstroCore;
import dev.malvaito.database.DatabaseManager;
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
            sender.sendMessage(plugin.miniMessage.deserialize("<red>Only players can use this command.</red>"));
            return true;
        }

        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();

        if (tpaManager.isOnCooldown(player.getUniqueId())) {
            long remainingTime = tpaManager.getRemainingCooldown(player.getUniqueId());
            player.sendMessage(plugin.miniMessage.deserialize("<red>You are on cooldown. Please wait " + remainingTime + " seconds.</red>"));
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
                    player.sendMessage(plugin.miniMessage.deserialize("<red>You cannot send a teleport request to yourself.</red>"));
                    return;
                }
                tpaManager.getTpaRequests().put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(plugin.miniMessage.deserialize("<green>You have sent a teleport request to <gold>" + target.getName() + "</gold>.</green>"));
                target.sendMessage(plugin.miniMessage.deserialize("<gold>" + player.getName() + "</gold> has sent you a teleport request. Use <green>/tpaccept</green> to accept or <red>/tpadeny</red> to deny."));
                tpaManager.addCooldown(player.getUniqueId(), 60 * 1000);
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>Player not found or is not online.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>Usage: /tpa <player></red>"));
        }
    }

    private void handleTpAcceptCommand(Player player) {
        if (tpaManager.getTpaRequests().containsKey(player.getUniqueId())) {
            UUID requesterUUID = tpaManager.getTpaRequests().remove(player.getUniqueId());
            Player requester = plugin.getServer().getPlayer(requesterUUID);
            if (requester != null && requester.isOnline()) {
                player.sendMessage(plugin.miniMessage.deserialize("<green>You have accepted the teleport request from <gold>" + requester.getName() + "</gold>.</green>"));
                requester.sendMessage(plugin.miniMessage.deserialize("<green>Your teleport request has been accepted by <gold>" + player.getName() + "</gold>. Teleporting in 5 seconds...</green>"));

                tpaManager.addTeleportTask(requester.getUniqueId(), plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (requester.isOnline() && tpaManager.getPlayerLocations().containsKey(requester.getUniqueId()) && tpaManager.getPlayerLocations().get(requester.getUniqueId()).equals(requester.getLocation())) {
                        requester.teleport(player.getLocation());
                        requester.sendMessage(plugin.miniMessage.deserialize("<green>You have been teleported to <gold>" + player.getName() + "</gold>.</green>"));
                    } else {
                        requester.sendMessage(plugin.miniMessage.deserialize("<red>Teleport cancelled because you moved or took damage.</red>"));
                    }
                    tpaManager.getTeleportTasks().remove(requester.getUniqueId());
                    tpaManager.getPlayerLocations().remove(requester.getUniqueId());
                }, 20L * 5), requester.getLocation());

                tpaManager.addCooldown(player.getUniqueId(), 5 * 1000);
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>The player who sent the request is no longer online.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>You have no pending teleport requests.</red>"));
        }
    }

    private void handleTpDenyCommand(Player player) {
        if (tpaManager.getTpaRequests().containsKey(player.getUniqueId())) {
            UUID requesterUUID = tpaManager.getTpaRequests().remove(player.getUniqueId());
            Player requester = plugin.getServer().getPlayer(requesterUUID);
            if (requester != null && requester.isOnline()) {
                player.sendMessage(plugin.miniMessage.deserialize("<red>You have denied the teleport request from <gold>" + requester.getName() + "</gold>.</red>"));
                requester.sendMessage(plugin.miniMessage.deserialize("<red>Your teleport request has been denied by <gold>" + player.getName() + "</gold>.</red>"));
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
                    player.sendMessage(plugin.miniMessage.deserialize("<red>You cannot send a teleport request to yourself.</red>"));
                    return;
                }
                tpaManager.getTpaRequests().put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(plugin.miniMessage.deserialize("<green>You have sent a request for <gold>" + target.getName() + "</gold> to teleport to you.</green>"));
                target.sendMessage(plugin.miniMessage.deserialize("<gold>" + player.getName() + "</gold> has sent you a request to teleport to them. Use <green>/tpaccept</green> to accept or <red>/tpadeny</red> to deny.</green>"));
                tpaManager.addCooldown(player.getUniqueId(), 60 * 1000);
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>Player not found or is not online.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>Usage: /tpahere <player></red>"));
        }
    }
}