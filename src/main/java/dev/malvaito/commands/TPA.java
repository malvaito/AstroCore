package dev.malvaito.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import dev.malvaito.AstroCore;
import dev.malvaito.database.DatabaseManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPA implements CommandExecutor, Listener {

    private final AstroCore plugin;
   private final Map<UUID, UUID> tpaRequests;
    private final Map<UUID, Long> cooldowns;
    private Map<UUID, BukkitTask> teleportTasks;
    private Map<UUID, org.bukkit.Location> playerLocations;
    public TPA(AstroCore plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.tpaRequests = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.teleportTasks = new HashMap<>();
        this.playerLocations = new HashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (teleportTasks.containsKey(playerId)) {
            Location from = playerLocations.get(playerId);
            Location to = event.getTo();
            if (from != null && to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())) {
                teleportTasks.get(playerId).cancel();
                teleportTasks.remove(playerId);
                playerLocations.remove(playerId);
                event.getPlayer().sendMessage(plugin.miniMessage.deserialize("<red>Teleport cancelled: You moved!</red>"));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();
            if (teleportTasks.containsKey(playerId)) {
                teleportTasks.get(playerId).cancel();
                teleportTasks.remove(playerId);
                playerLocations.remove(playerId);
                player.sendMessage(plugin.miniMessage.deserialize("<red>Teleport cancelled: You took damage!</red>"));
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.miniMessage.deserialize("<red>Only players can use this command.</red>"));
            return true;
        }

        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();

        if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
            long remainingTime = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
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
        if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
            long remainingTime = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            player.sendMessage(plugin.miniMessage.deserialize("<red>You are on cooldown. Please wait " + remainingTime + " seconds.</red>"));
            return;
        }

        if (args.length == 1) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.miniMessage.deserialize("<red>You cannot send a teleport request to yourself.</red>"));
                    return;
                }
                tpaRequests.put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(plugin.miniMessage.deserialize("<green>You have sent a teleport request to <gold>" + target.getName() + "</gold>.</green>"));
                target.sendMessage(plugin.miniMessage.deserialize("<gold>" + player.getName() + "</gold> has sent you a teleport request. Use <green>/tpaccept</green> to accept or <red>/tpadeny</red> to deny."));
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (60 * 1000));
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>Player not found or is not online.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>Usage: /tpa <player></red>"));
        }
    }

    private void handleTpAcceptCommand(Player player) {
        if (tpaRequests.containsKey(player.getUniqueId())) {
            UUID requesterUUID = tpaRequests.remove(player.getUniqueId());
            Player requester = plugin.getServer().getPlayer(requesterUUID);
            if (requester != null && requester.isOnline()) {
                player.sendMessage(plugin.miniMessage.deserialize("<green>You have accepted the teleport request from <gold>" + requester.getName() + "</gold>.</green>"));
                requester.sendMessage(plugin.miniMessage.deserialize("<green>Your teleport request has been accepted by <gold>" + player.getName() + "</gold>. Teleporting in 5 seconds...</green>"));

                playerLocations.put(requester.getUniqueId(), requester.getLocation());
                teleportTasks.put(requester.getUniqueId(), plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (requester.isOnline() && playerLocations.containsKey(requester.getUniqueId()) && playerLocations.get(requester.getUniqueId()).equals(requester.getLocation())) {
                        requester.teleport(player.getLocation());
                        requester.sendMessage(plugin.miniMessage.deserialize("<green>You have been teleported to <gold>" + player.getName() + "</gold>.</green>"));
                    } else {
                        requester.sendMessage(plugin.miniMessage.deserialize("<red>Teleport cancelled because you moved or took damage.</red>"));
                    }
                    teleportTasks.remove(requester.getUniqueId());
                    playerLocations.remove(requester.getUniqueId());
                }, 20L * 5));

                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (5 * 1000));
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>The player who sent the request is no longer online.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>You have no pending teleport requests.</red>"));
        }
    }

    private void handleTpDenyCommand(Player player) {
        if (tpaRequests.containsKey(player.getUniqueId())) {
            UUID requesterUUID = tpaRequests.remove(player.getUniqueId());
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
        if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
            long remainingTime = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            player.sendMessage(plugin.miniMessage.deserialize("<red>You are on cooldown. Please wait " + remainingTime + " seconds.</red>"));
            return;
        }

        if (args.length == 1) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.miniMessage.deserialize("<red>You cannot send a teleport request to yourself.</red>"));
                    return;
                }
                tpaRequests.put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(plugin.miniMessage.deserialize("<green>You have sent a request for <gold>" + target.getName() + "</gold> to teleport to you.</green>"));
                target.sendMessage(plugin.miniMessage.deserialize("<gold>" + player.getName() + "</gold> has sent you a request to teleport to them. Use <green>/tpaccept</green> to accept or <red>/tpadeny</red> to deny.</green>"));
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (60 * 1000));
            } else {
                player.sendMessage(plugin.miniMessage.deserialize("<red>Player not found or is not online.</red>"));
            }
        } else {
            player.sendMessage(plugin.miniMessage.deserialize("<red>Usage: /tpahere <player></red>"));
        }
    }
}
