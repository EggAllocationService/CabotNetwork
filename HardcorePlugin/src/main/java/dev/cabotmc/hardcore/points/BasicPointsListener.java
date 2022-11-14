package dev.cabotmc.hardcore.points;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;


public class BasicPointsListener implements Listener {
    HashMap<EntityType, Integer> limitMap = new HashMap<>();
    @EventHandler
    public void blocks(EntityPickupItemEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        var p = (Player) e.getEntity();
        var m = e.getItem().getItemStack().getType();
        if (m.toString().endsWith("LOG")) {
            PointsManager.addKeyed("Gathered wood", 0.25f);
        } else if (m == Material.DIAMOND) {
            PointsManager.addKeyed("Diamonds!", 5f);
        } else if (m == Material.RAW_IRON) {
            PointsManager.addKeyed("Your first iron", 1f);
        } else if (m == Material.DRAGON_EGG) {
            PointsManager.addKeyed("Collect the dragon egg", 30);
        } else if (m == Material.NETHER_STAR) {
            PointsManager.addKeyed("Collect a nether star", 30);
        } else if (m == Material.WITHER_SKELETON_SKULL) {
            PointsManager.addKeyed("Collect a wither skull", 10);
        }
    }
    
    @EventHandler
    public void killEntity(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        if (!limitMap.containsKey(e.getEntityType())) {
            limitMap.put(e.getEntityType(), 1);
        }
        float pts = (float) (e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * (0.1) * (1.0f / limitMap.get(e.getEntityType())));
        if (!(e.getEntity() instanceof Monster)) {
            pts = pts / 2;
        }
        pts = (float) Math.round(pts * 100) / 100;
        if (pts <= 0.01) {
            return;
        }
        limitMap.put(e.getEntityType(), limitMap.get(e.getEntityType()) + 1);
        PointsManager.addPoints("Killed " + e.getEntity().getName(), pts);
    }
    @EventHandler
    public void switchDim(PlayerChangedWorldEvent e) {
        if (e.getPlayer().getWorld().getName().endsWith("nether")) {
            PointsManager.addKeyed("Entered the nether", 7.5f);
        } else if (e.getPlayer().getWorld().getName().endsWith("end")) {
            PointsManager.addKeyed("Entered the end", 12f);
        }
    }
}
