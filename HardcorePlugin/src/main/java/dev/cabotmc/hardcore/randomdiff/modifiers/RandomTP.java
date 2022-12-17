package dev.cabotmc.hardcore.randomdiff.modifiers;

import java.time.Instant;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class RandomTP extends Modifier implements Listener {
    long nextTp;
    long nextSave;
    Location savedLocation;

    public RandomTP() {
        super("D\u00e9j\u00e0 vu");
    }

    @Override
    public Component generateDescription() {
        return Component.text("You randomly teleport to your position ten seconds ago", TextColor.color(Modifier.BAD_COLOR));
    }

    @Override
    public void activate() {
        randomizeTimers();
    }

    @EventHandler
    public void tick(ServerTickEndEvent e) {
        if (nextSave != 0 && currentTime() >= nextSave) {
            // overdue for a save
            var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
            savedLocation = p.getLocation();
            nextSave = 0;
        }
        if (nextTp != 0 && currentTime() >= nextTp) {
            // time to tp
            final var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 20, 1, true, false));
            p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, SoundCategory.MASTER, 0.7f, 1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(HardcorePlugin.instance, () -> {
                p.teleport(savedLocation);
                p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, SoundCategory.MASTER, 0.7f, 1);
                p.removePotionEffect(PotionEffectType.CONFUSION);
                randomizeTimers();
                
            }, 5 * 20);
            nextTp = 0;
        }
    }
    void randomizeTimers() {
        nextSave = ((long) Math.floor(Math.random() * 30000)) + 120000 + currentTime();
        nextTp = nextSave + 5000;
    }
    long currentTime() {
        return Instant.now().toEpochMilli();
    }

}