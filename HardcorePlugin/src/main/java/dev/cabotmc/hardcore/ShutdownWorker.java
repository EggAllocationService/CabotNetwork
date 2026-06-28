package dev.cabotmc.hardcore;

import com.google.common.io.ByteStreams;
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
    static int ticksLeft = 20 * 20;

    public static void start() {
        Bukkit.getOnlinePlayers().forEach(p -> p.showBossBar(shutdownBar));
        Bukkit.getScheduler().scheduleSyncRepeatingTask(HardcorePlugin.instance, () -> {
            ticksLeft--;
            if (ticksLeft == 20) {
                var pkt = ByteStreams.newDataOutput();
                pkt.writeUTF("lobby");
                var data = pkt.toByteArray();
                Bukkit.getOnlinePlayers().forEach(p -> p.sendPluginMessage(HardcorePlugin.instance, "cabot:connect", data));
            }
            if (ticksLeft <= 0) {
                Bukkit.shutdown();
            } else {
                shutdownBar.progress(ticksLeft / (20.0f * 20.0f));
            }
        }, 0, 1);
    }

    static Component createTitle() {
        return Component.text("Server is shutting down in 20 seconds", TextColor.color(249, 79, 64));
    }
}
