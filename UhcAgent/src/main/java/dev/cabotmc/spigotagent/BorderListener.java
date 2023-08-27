package dev.cabotmc.spigotagent;


import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.TitlePart;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.event.player.PlayerRespawnEvent;

public class BorderListener implements Listener {
    public HashMap<UUID, Integer> borderTicksRemaining = new HashMap<>();
    public HashMap<UUID, Integer> borderTicksResetTimers = new HashMap<>();
    int tickCount = 0;

    int colorIndex = 0;
    int[] colors = new int[]{0xeb3434, 0xeb8334};
    @EventHandler
    public void tick(ServerTickStartEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() != GameMode.SURVIVAL || p.isDead()) continue;
            var b = p.getWorld().getWorldBorder();
            if (!b.isInside(p.getLocation())) {
                // player is outside zone
                borderTicksRemaining.put(p.getUniqueId(), borderTicksRemaining.get(p.getUniqueId()) - 1);
                showWarningTitle(p);
                if (borderTicksRemaining.get(p.getUniqueId()) < 0) {
                    p.damage(1000);
                }
                if (borderTicksResetTimers.containsKey(p.getUniqueId())) {
                    Bukkit.getScheduler().cancelTask(borderTicksResetTimers.get(p.getUniqueId()));
                    borderTicksResetTimers.remove(p.getUniqueId());
                }
            } else {
                // player in zone
                if (borderTicksRemaining.get(p.getUniqueId()) != 140 && !borderTicksResetTimers.containsKey(p.getUniqueId()))  {
                    // start regen after 10
                    var taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(SpigotAgent.instance, () -> {
                        borderTicksRemaining.put(p.getUniqueId(), 140);
                    }, 20 * 10);
                    borderTicksResetTimers.put(p.getUniqueId(), taskId);
                }
            }
        }

        tickCount ++;
        if (tickCount > 10) {
            tickCount = 0;
            if (colorIndex > 0) {
                colorIndex = 0;
            } else {
                colorIndex = 1;
            }
        }
    }
    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (!borderTicksRemaining.containsKey(e.getPlayer().getUniqueId())) {
            borderTicksRemaining.put(e.getPlayer().getUniqueId(), 140);
        }
    }
    @EventHandler
    public void respawn(PlayerRespawnEvent e) {
        borderTicksRemaining.put(e.getPlayer().getUniqueId(), 140);

    }
    static Times WARNING_TIMES = Times.times(Duration.ofMillis(0), Duration.ofMillis(200), Duration.ofMillis(0));
    private void showWarningTitle(Player p) {

        var remainingTime = borderTicksRemaining.get(p.getUniqueId()) / 20f;
        remainingTime = remainingTime * 1000;
        remainingTime = (float) Math.floor(remainingTime);
        remainingTime = remainingTime / 1000;
        var remString = "" + remainingTime;
        if (remString.length() == 3) {
            remString += "0";
        }
        var sub = Component.text("WARNING: Get back in the zone " + remString, TextColor.color(colors[colorIndex]));
        p.showTitle(
                Title.title(
                        Component.text(""),
                        sub,
                        WARNING_TIMES
                )
        );

    }
}
