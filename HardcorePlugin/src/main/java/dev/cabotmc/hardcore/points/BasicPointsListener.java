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
import org.bukkit.event.entity.EntityTameEvent;
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
            PointsManager.addKeyed("Collect the dragon egg", 40);
        } else if (m == Material.NETHER_STAR) {
            PointsManager.addKeyed("Collect a nether star", 40);
        } else if (m == Material.WITHER_SKELETON_SKULL) {
            PointsManager.addKeyed("Collect a wither skull", 10);
        }
    }
    
    @EventHandler
    public void killEntity(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null || !(e.getEntity().getKiller() instanceof Player)) return;
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
        var p = (Player) e.getEntity().getKiller();
        int multiplier = 2;
        if (p.getLocation().distance(e.getEntity().getLocation()) > 50) {
            PointsManager.addPoints("(" + multiplier + ".0x) Long-range kill!", pts, -0x444444);
            multiplier++;
        }
        if (e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() > 20.0f &&  p.getInventory().getItemInMainHand().getType() == Material.WOODEN_SWORD) {
            PointsManager.addPoints("(" + multiplier + ".0x) Killed a high-health target with a wooden sword", pts, -0x444444);
            multiplier++;
        }
        if (p.getHealth() <= 4) {
            PointsManager.addPoints("(" + multiplier + ".0x) Low health", pts, -0x444444);
            multiplier++;
        }
        if (hasNoArmor(p) && hasNoShield(p)) {
            PointsManager.addPoints("(" + (multiplier -1) + ".5x) No armor and no shield", pts / 2, -0x444444);
            multiplier++;
        }
        if (e.getEntity().hasMetadata("extra_points")) {
            int xtra = (int) e.getEntity().getMetadata("extra_points").get(0).value();
            PointsManager.addPoints("Special enemy bonus", xtra, -0x444444);
        }
    }
    static boolean hasNoArmor(Player p) {
        var i = p.getInventory();
        return i.getHelmet() == null && i.getChestplate() == null && i.getLeggings() == null && i.getBoots() == null;
    }
    static boolean hasNoShield(Player p) {
        var i = p.getInventory();
        var hasShield = false;
        if (i.getItemInMainHand() != null && i.getItemInMainHand().getType() == Material.SHIELD) {
            hasShield = true;
        }
        if (i.getItemInOffHand() != null && i.getItemInMainHand().getType() == Material.SHIELD) {
            hasShield = true;
        }
        return !hasShield;
    }
    @EventHandler
    public void switchDim(PlayerChangedWorldEvent e) {
        if (e.getPlayer().getWorld().getName().endsWith("nether")) {
            PointsManager.addKeyed("Entered the nether", 10f);
        } else if (e.getPlayer().getWorld().getName().endsWith("end")) {
            PointsManager.addKeyed("Entered the end", 20f);
        }
    }
    @EventHandler
    public void tame(EntityTameEvent e) {
        if (!(e.getOwner() instanceof Player)) return;
        var p = (Player) e.getOwner();
        if (e.getEntityType() == EntityType.CAT) {
            PointsManager.addKeyed("Tamed a cat", 5);
        } else if (e.getEntityType() == EntityType.WOLF) {
            PointsManager.addKeyed("Tamed a dog", 5);
        } else if (e.getEntityType() == EntityType.PARROT) {
            PointsManager.addKeyed("Tamed a parrot", 5);
        }
    }
}
