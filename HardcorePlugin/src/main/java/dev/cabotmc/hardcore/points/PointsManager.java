package dev.cabotmc.hardcore.points;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PointsManager {
    public static float points = 0.0f;
    public static boolean enabled = true;
    public static BossBar displayBar;
    static HashSet<String> earnedKeys = new HashSet<>();
    public static void init() {
        displayBar = BossBar.bossBar(createTitle(), points, Color.YELLOW, Overlay.NOTCHED_10);
    }

    public static void addPoints(float pts) {
        if (!enabled) return;
        points += pts;
        points = (float) Math.round(points * 100) / 100;
        displayBar.name(createTitle());
        displayBar.progress((float) (points - Math.floor(points)));
    }
    public static void addPoints(String reason, float pts) {
        addPoints(pts);
        var msg = Component.text(" +" + pts, TextColor.color(0xc7e327));
            msg = msg.append(Component.text(" " +reason, TextColor.color(0x909c27)));
            Bukkit.getServer().sendMessage(msg);
    }
    public static void addKeyed(String key, float pts) {
        if (!earnedKeys.contains(key)) {
            earnedKeys.add(key);
            addPoints(key, pts);
            
        }
    }
    public static Component createTitle() {
        return Component.text("Current points earned: " + points, TextColor.color(220, 220, 80));
    }
}
