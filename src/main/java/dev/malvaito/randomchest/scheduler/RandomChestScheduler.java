package dev.malvaito.randomchest.scheduler;

import dev.malvaito.randomchest.RandomChest;
import org.bukkit.scheduler.BukkitRunnable;

public class RandomChestScheduler extends BukkitRunnable {

    private final RandomChest randomChest;

    public RandomChestScheduler(RandomChest randomChest) {
        this.randomChest = randomChest;
    }

    @Override
    public void run() {
        
        randomChest.generateRandomChest();
    }
}