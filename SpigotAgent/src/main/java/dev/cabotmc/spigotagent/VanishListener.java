package dev.cabotmc.spigotagent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.cabotmc.vanish.VanishManager;
import net.kyori.adventure.text.Component;


public class VanishListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent e) {
        if (VanishManager.isVanished(e.getPlayer().getUniqueId())) {
            e.joinMessage(null);
            SpigotAgent.v.vanishPlayer(e.getPlayer().getUniqueId());
            e.getPlayer().sendMessage(Component.text("Backend acknowledges you are vanished."));
        } else if (!e.getPlayer().hasPermission("vanish.see")) {
            var vanishedPlayers = VanishManager.getVanishedPlayers();
            vanishedPlayers.stream()
                .map(Bukkit::getPlayer)
                .filter(c -> c != null)
                .forEach(p -> {
                    e.getPlayer().hidePlayer(SpigotAgent.getPlugin(SpigotAgent.class), p);
                }); 
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void leave(PlayerQuitEvent e) {
        if (VanishManager.isVanished(e.getPlayer().getUniqueId())) {
            e.quitMessage(null);
        }
    }
    @EventHandler
    public void target(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player) {
            var p = (Player) e.getTarget();
            if (VanishManager.isVanished(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void damage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            var p = (Player) e.getEntity();
            if (VanishManager.isVanished(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void hunger(FoodLevelChangeEvent e) {
        var p = e.getEntity();
        if (VanishManager.isVanished(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
