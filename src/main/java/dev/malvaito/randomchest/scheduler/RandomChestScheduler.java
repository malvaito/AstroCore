package dev.malvaito.randomchest.scheduler;

import dev.malvaito.randomchest.RandomChest;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class RandomChestScheduler extends BukkitRunnable {

    private final RandomChest randomChest;

    public RandomChestScheduler(RandomChest randomChest) {
        this.randomChest = randomChest;
    }

    @Override
    public void run() {
        // Lógica para generar un cofre aleatorio
        // Por ahora, solo un mensaje para confirmar que funciona
        Bukkit.broadcastMessage("¡Un cofre aleatorio ha aparecido en algún lugar!");
        // Aquí se debería llamar a un método de randomChest para generar un cofre
        // Por ejemplo: randomChest.generateRandomChest();
    }
}