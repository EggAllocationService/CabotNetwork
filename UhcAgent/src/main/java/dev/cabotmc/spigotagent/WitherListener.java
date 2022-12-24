package dev.cabotmc.spigotagent;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcTeam;

public class WitherListener implements Listener {
    public static HashSet<PlacedSkull> skulls = new HashSet<>();

    @EventHandler
    public void place(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.WITHER_SKELETON_SKULL
                || e.getBlock().getType() == Material.WITHER_SKELETON_WALL_SKULL) {
            skulls.add(new PlacedSkull(e));
        }
    }

    @EventHandler
    public void broken(BlockBreakEvent e) {
        var found = skulls.stream()
                .filter(c -> c.sameLocation(e.getBlock().getLocation()))
                .toList();
        if (found.size() != 0) {
            skulls.removeAll(found);
        }
    }

    @EventHandler
    public void spawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == SpawnReason.BUILD_WITHER) {
            var w = (Wither) e.getEntity();
            var candidates = skulls.stream()
                    .filter(skull -> skull.isInvolvedInSpawnAt(e.getLocation()))
                    .toList();
            skulls.removeAll(candidates);
            var top = candidates.stream()
                    .reduce(null, (a, b) -> a != null ? a.getLatest(b) : b);
            var targetTeam = getTeamForPlayer(top.owner);
            if (targetTeam == null) {
                System.out.println("WARN: Couldnt figure out who spawned a wither!");
                return;
            }
            w.setMetadata("minion_owner", new FixedMetadataValue(SpigotAgent.instance, targetTeam.getMembersNames()));
        }
    }

    @EventHandler
    public void target(EntityTargetEvent e) {
        if (e.getEntityType() == EntityType.WITHER) {
            if (e.getTarget() == null)
                return;
            if (!(e.getTarget() instanceof Player)) {
                e.setCancelled(true);
                return;
            }
            var p = (Player) e.getTarget();
            var m = e.getEntity().getMetadata("minion_owner");
            for (var x : m) {
                List<String> names = (List<String>) x.value();
                if (names.contains(p.getName())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        Entity attacker = null;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (e.getEntity() instanceof Projectile) {
            var s = ((Projectile) e.getEntity()).getShooter();
            if (s instanceof Entity) {
                attacker = (Entity) s;
            }
        } else if (e.getEntity() instanceof Wither) {
            attacker = e.getEntity();
        } else {
            return;
        }
        if (attacker == null || !(attacker instanceof Wither))
            return;
        var m = attacker.getMetadata("minion_owner");
        for (var x : m) {
            List<String> names = (List<String>) x.value();
            if (names.contains(p.getName())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    public UhcTeam getTeamForPlayer(Player p) {
        for (var t : GameManager.getGameManager().getTeamManager().getUhcTeams()) {
            if (t.getMembersNames().contains(p.getName())) {
                return t;
            }
        }
        return null;
    }

    public static class PlacedSkull {
        Player owner;
        Location blockPos;
        long timePlaced;

        public PlacedSkull(BlockPlaceEvent e) {
            owner = e.getPlayer();
            blockPos = e.getBlock().getLocation();
            timePlaced = Instant.now().toEpochMilli();
        }

        public boolean isInvolvedInSpawnAt(Location other) {
            return other.getWorld().equals(blockPos.getWorld()) && other.distance(blockPos) <= 1.5;
        }

        public boolean sameLocation(Location l) {
            return l.equals(blockPos);
        }

        public PlacedSkull getLatest(PlacedSkull other) {
            if (other == null)
                return this;
            return other.timePlaced < timePlaced ? this : other;
        }

        @Override
        public int hashCode() {
            return blockPos.hashCode();
        }
    }
}
