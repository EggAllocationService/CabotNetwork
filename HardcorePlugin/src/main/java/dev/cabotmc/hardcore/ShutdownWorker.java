package dev.cabotmc.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ShutdownWorker {
    static BossBar shutdownBar = BossBar.bossBar(createTitle(), 1.0f, Color.RED, Overlay.NOTCHED_20);
    static float secondsLeft = 20.0f;

    public static void start() {
        Bukkit.getOnlinePlayers().forEach(p -> p.showBossBar(shutdownBar));
        Bukkit.getScheduler().scheduleSyncRepeatingTask(HardcorePlugin.instance, () -> {
            secondsLeft = secondsLeft - 0.05f;
            if (Math.floor(secondsLeft) == secondsLeft) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.8f, secondsLeft % 2 == 0 ? 1.0f : 1.5f);
                }); 
            }
            
            if (secondsLeft <= 0f) {
                Bukkit.shutdown();
            } else {
                shutdownBar.progress(secondsLeft / 20.0f);
            }
        }, 0, 1);
    }

    static Component createTitle() {
        return Component.text("Server is shutting down in 20 seconds", TextColor.color(249, 79, 64));
    }
}
