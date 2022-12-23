package dev.cabotmc.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import dev.cabotmc.hardcore.difficulty.DifficultyMenu;
import dev.cabotmc.hardcore.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public class BasicListener implements Listener {
    @EventHandler
    public void rejectPrematureJoins(PlayerLoginEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().equals(HardcorePlugin.ownerName) && HardcorePlugin.allowSpectators) {
                return;
            }
        }
        if (!e.getPlayer().getName().equals(HardcorePlugin.ownerName)) {
            e.kickMessage(Component.text("Either the owner has not logged in, or they have disabled spectating on this server"));
            e.setResult(Result.KICK_OTHER);
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        e.getPlayer().showBossBar(PointsManager.displayBar);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tpsbar " + e.getPlayer().getName());
        if (!e.getPlayer().getName().equals(HardcorePlugin.ownerName)) {
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().setInvulnerable(true);
            e.getPlayer().setSilent(true);
            e.joinMessage(
                    Component.text(e.getPlayer().getName() + " started spectating", TextColor.color(255, 255, 85)));
            var owner = Bukkit.getPlayer(HardcorePlugin.ownerName);
            for (var p : Bukkit.getOnlinePlayers()) {
                if (p.equals(owner))
                    continue;
                owner.hidePlayer(HardcorePlugin.instance, p);
            }
            HardcorePlugin.SPECTATOR_TEAM.addEntity(e.getPlayer());
            e.getPlayer().setAllowFlight(true);
            e.getPlayer().teleport(Bukkit.getPlayerExact(HardcorePlugin.ownerName));
            e.getPlayer().displayName(Component.text("Spectator").color(TextColor.color(0x444444))
                    .append(Component.text(" | " + e.getPlayer().getName()).color(TextColor.color(0xFFFFFF))));
            e.getPlayer().getInventory().setItem(8, HardcorePlugin.TELEPORT_STACK);
            e.getPlayer().setAffectsSpawning(false);
            e.getPlayer().setSleepingIgnored(true);
            if (HardcorePlugin.difficulty != null) {
                for (Component c : DifficultyMenu.createDesc(HardcorePlugin.difficulty)) {
                    e.getPlayer().sendMessage(c);
                }
            }
        } else {
            e.joinMessage(null);
            var w = e.getPlayer().getWorld();
            w.getWorldBorder().setCenter(e.getPlayer().getLocation());
            w.getWorldBorder().setSize(9d);
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().setInvulnerable(true);
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 10, true, false));
            w.getWorldBorder().setWarningDistance(0);
            w.getWorldBorder().setWarningTime(0);
            w.getChunkAtAsync(e.getPlayer().getLocation().clone().subtract(64, 0, 64), true)
                    .thenAccept(c -> {
                       HardcorePlugin.world_ready = true;
                       HardcorePlugin.instance.tryActivate();
                    });

            Bukkit.getServer()
                    .showTitle(Title.title(
                            Component.text("Pregenerating world, please wait...", TextColor.color(0xFA6655)),
                            Component.text("This may take up to a minute")));
            Database.notifyBest();
            new DifficultyMenu().open(e.getPlayer());
        }
    }

    @EventHandler
    public void leave(PlayerQuitEvent e) {
        if (e.getPlayer().getName().equals(HardcorePlugin.ownerName)) {
            Bukkit.shutdown();
        } else {
            e.quitMessage(Component.text(e.getPlayer().getName() + " stopped spectating", TextColor.color(255, 255, 85)));
        }
    }

    @EventHandler
    public void dimensionSwitch(PlayerTeleportEvent e) {
        if (e.getFrom().getWorld().equals(e.getTo().getWorld())) return;
        if (e.getCause() == TeleportCause.PLUGIN) return;
        if (e.getCause() != TeleportCause.END_GATEWAY && e.getCause() != TeleportCause.END_PORTAL && e.getCause() != TeleportCause.NETHER_PORTAL && e.getCause() != TeleportCause.ENDER_PEARL) return;
        if (e.getCause() == TeleportCause.ENDER_PEARL && !e.getTo().getWorld().getName().contains("end")) {
            //stop if the ender pearl didnt happen in the end
            return;
        }
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true); // prevent portal travel for spectators
            return;
        }
        if (p.getName().equals(HardcorePlugin.ownerName)) {
            for (Player b : Bukkit.getOnlinePlayers()) {
                if (p == b) continue;
                b.teleport(p);
            }
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent e) {
        e.setCancelled(true);
        if (!e.getPlayer().getName().equals(HardcorePlugin.ownerName))
            return;
        float ptsToAdd = e.getPlayer().getLevel() + e.getPlayer().getExp();
        var msg = Component.text(" +" + ptsToAdd, TextColor.color(0xc7e327));
            msg = msg.append(Component.text(" XP To Points", TextColor.color(0x909c27)));
            Bukkit.getServer().sendMessage(msg);
        PointsManager.addPoints(ptsToAdd);
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
        e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), e.getDeathSound(), 1.5f,
                e.getDeathSoundPitch());
        
        PointsManager.enabled = false;
        ShutdownWorker.start();
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tpsbar " + e.getPlayer().getName());
        Bukkit.getServer().showTitle(Title.title(
                Component.text("YOU DIED", Style.style(TextColor.color(0xFA8664), TextDecoration.BOLD)),
                Component.text("Server shutting down in 20 seconds")));
        e.getPlayer().setInvulnerable(true);
        e.getPlayer().setFireTicks(0);
        for (Entity a : e.getPlayer().getNearbyEntities(32, 32, 32)) {
            if (a instanceof Mob) {
                var m = (Mob) a;
                if (m.getTarget() == null) continue;
                if (m.getTarget().equals(e.getPlayer())) {
                    m.setTarget(null);
                }
            }
        }
        Database.notifyIfBetter();
        Database.updateScore();
    }

    @EventHandler
    public void pickupPreventer(PlayerAttemptPickupItemEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
   
    @EventHandler
    public void adventureInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
            if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey("cabot", "tpitem"))) {
                var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
                e.getPlayer().teleport(p);      
            }
        }
    }
    @EventHandler
    public void adventureDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void adventureXp(PlayerPickupExperienceEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            var p = (Player) e.getDamager();
            if (p.getGameMode() == GameMode.ADVENTURE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void target(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player))
            return;
        if (((Player) e.getTarget()).getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void hunger(FoodLevelChangeEvent e) {
        if (e.getEntity().getGameMode() == GameMode.ADVENTURE) {
            e.setCancelled(true);
        }
    }
}
