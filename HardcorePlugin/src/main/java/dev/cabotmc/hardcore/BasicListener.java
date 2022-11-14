package dev.cabotmc.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;

import dev.cabotmc.hardcore.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public class BasicListener implements Listener {
    @EventHandler
    public void join(PlayerJoinEvent e) {
        e.getPlayer().showBossBar(PointsManager.displayBar);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tpsbar " + e.getPlayer().getName());
        if (!e.getPlayer().getName().equals(HardcorePlugin.ownerName)) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().scheduleSyncDelayedTask(HardcorePlugin.instance, () -> {
                e.getPlayer().setSpectatorTarget(Bukkit.getPlayerExact(HardcorePlugin.ownerName));
            }, 5);
            e.joinMessage(Component.text(e.getPlayer().getName() + " started spectating", TextColor.color(255, 255, 85)));
        } else {
            e.joinMessage(null);
            var w = e.getPlayer().getWorld();
            w.getWorldBorder().setCenter(e.getPlayer().getLocation());
            w.getWorldBorder().setSize(9d);
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 10, true, false));
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 10, true, false));
            w.getWorldBorder().setWarningDistance(0);
            w.getWorldBorder().setWarningTime(0);
            w.getChunkAtAsync(e.getPlayer().getLocation().clone().subtract(64, 0, 64), true)
                .thenAccept(c -> {
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
                    w.getWorldBorder().setSize(400, 40);
                    e.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                    e.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
                    e.getPlayer().setGameMode(GameMode.SURVIVAL);
                    Bukkit.getScheduler().runTaskLater(HardcorePlugin.instance, () -> {
                        w.getWorldBorder().setSize(320000);
                    }, 40 * 20);
                });
                
            Bukkit.getServer().showTitle(Title.title(Component.text("Pregenerating world, please wait...", TextColor.color(0xFA6655)), Component.text("This may take up to a minute")));
        }
    }
    @EventHandler
    public void forcedSpectating(PlayerStopSpectatingEntityEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void leave(PlayerQuitEvent e) {
        if (e.getPlayer().getName().equals(HardcorePlugin.ownerName)) {
            Bukkit.shutdown();
        }
    }
    @EventHandler
    public void death(PlayerDeathEvent e) {
        e.setCancelled(true);
        if (!e.getPlayer().getName().equals(HardcorePlugin.ownerName)) return;
        Bukkit.broadcast(e.deathMessage());
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.getDrops().forEach(d -> {
            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), d);
        });
        e.getPlayer().getInventory().clear();
        e.getPlayer().setAllowFlight(true);
        e.getPlayer().setFlying(true);
        e.getPlayer().closeInventory();
        
        e.getPlayer().setVelocity(new Vector(0.0f, 0.8f, 0.0f));
        e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), e.getDeathSound(), 1.5f, e.getDeathSoundPitch());
        PointsManager.enabled = false;
        ShutdownWorker.start();
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tpsbar " + e.getPlayer().getName());
        Bukkit.getServer().showTitle(Title.title(
            Component.text("YOU DIED", Style.style(TextColor.color(0xFA8664), TextDecoration.BOLD)), 
            Component.text("Server shutting down in 20 seconds")
            ));
        e.getPlayer().setInvulnerable(true);
        for (Entity a : e.getPlayer().getNearbyEntities(32, 32, 32)) {
            if (a instanceof Mob) {
                var m = (Mob) a;
                if (m.getTarget().equals(e.getPlayer())) {
                    m.setTarget(null);
                }
            }
        }
    }
    @EventHandler
    public void pickupPreventer(PlayerAttemptPickupItemEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void target(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) return;
        if (((Player) e.getTarget()).getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
}
