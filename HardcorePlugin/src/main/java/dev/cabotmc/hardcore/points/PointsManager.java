package dev.cabotmc.hardcore.points;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import dev.cabotmc.hardcore.HardcorePlugin;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PointsManager {
    public static float points = 0.0f;
    static ArrayList<Function<Double, Double>> modifiers = new ArrayList<>();
    public static boolean enabled = true;
    public static BossBar displayBar;
    static HashSet<String> earnedKeys = new HashSet<>();
    
    public static void init() {
        displayBar = BossBar.bossBar(createTitle(), points, Color.YELLOW, Overlay.NOTCHED_10);
    }

    public static void addPoints(float pts) {
        if (!enabled) return;
        points += pts ;
        points = (float) Math.round(points * 100) / 100;
        displayBar.name(createTitle());
        displayBar.progress((float) (points - Math.floor(points)));
    }
    public static void addPoints(String reason, float pts, int colorOffset) {
        var multiplier = HardcorePlugin.difficulty.getMultiplier();
        if (colorOffset == 0) {
            for (var f : modifiers) {
                multiplier = f.apply(multiplier);
            }
        }
        pts = (float) (pts * multiplier);
        addPoints(pts);
        pts = (float) Math.round(pts * 100) / 100;
        if (pts == 0) return;
        var msg = Component.text(" +" + pts, TextColor.color(0xc7e327 + colorOffset));
            msg = msg.append(Component.text(" " +reason, TextColor.color(0x909c27 + colorOffset)));
            Bukkit.getServer().sendMessage(msg);
    }
    public static void addPoints(String reason, float pts) {
        addPoints(reason, pts, 0);
    }
    public static void addKeyed(String key, float pts) {
        if (!earnedKeys.contains(key)) {
            earnedKeys.add(key);
            addPoints(key, pts);
            
        }
    }
    public static void addMutator(Function<Double, Double> callback) {
        modifiers.add(callback);
    }
    public static Component createTitle() {
        return Component.text("Current points earned: " + points, TextColor.color(220, 220, 80));
    }
    public static void round() {
        var x = points * 100;
        x = Math.round(x);
        points = x / 100;
    }
}
