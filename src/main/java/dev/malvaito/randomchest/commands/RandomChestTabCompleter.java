package dev.malvaito.randomchest.commands;

import org.bukkit.command.Command;
import dev.malvaito.randomchest.RandomChest;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RandomChestTabCompleter implements TabCompleter {

    private final RandomChest randomChest;

    public RandomChestTabCompleter(RandomChest randomChest) {
        this.randomChest = randomChest;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "additem", "spawn"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("additem") || args[0].equalsIgnoreCase("spawn")) {
                Set<String> chestNames = randomChest.getChestConfig().getConfig().getConfigurationSection("chests").getKeys(false);
                completions.addAll(chestNames);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("additem")) {
                completions.addAll(Arrays.asList("hand", "gui"));
            } else if (args[0].equalsIgnoreCase("spawn")) {
                completions.addAll(Arrays.asList("here", "random"));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}