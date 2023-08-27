package dev.cabotmc.spigotagent.beacon;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.gmail.val59000mc.game.GameManager;
import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BeaconListener implements Listener {
  public HashMap<Location, Integer> blockTeams = new HashMap();
  public HashSet<Location> activeBeacons = new HashSet<>();

  public void beaconPlaced(Block beacon, Player placer) {
    var num = GameManager.getGameManager().getPlayerManager().getOrCreateUhcPlayer(placer).getTeam().getTeamNumber();
    blockTeams.put(beacon.getLocation().toBlockLocation(), num);
  }

  @EventHandler
  public void blockPlaced(BlockPlaceEvent e) {
    if (e.getBlockPlaced().getType() != Material.BEACON) return;
    beaconPlaced(e.getBlockPlaced(), e.getPlayer());
  }

  @EventHandler
  public void destroy(BlockDestroyEvent e) {
    if (e.getBlock() instanceof Beacon) {
      blockTeams.remove(e.getBlock().getLocation().toBlockLocation());
    }
  }

  @EventHandler
  public void beaconActivated(BeaconActivatedEvent e) {
    activeBeacons.add(e.getBeacon().getLocation().toBlockLocation());
  }

  @EventHandler
  public void beaconDeactivate(BeaconDeactivatedEvent e) {
    activeBeacons.remove(e.getBlock().getLocation().toBlockLocation());
  }

  static final Title.Times PROTECTED_TIMES = Title.Times.times(
          Duration.ofMillis(0),
          Duration.ofMillis(100),
          Duration.ofMillis(0)
  );
  static final Title PROTECTED_TITLE = Title.title(
          Component.empty(),
          Component.text("[Beacon Protected]")
                  .color(TextColor.color(0x39fc03)),
          PROTECTED_TIMES
  );

  @EventHandler(priority = EventPriority.LOWEST)
  public void death(PlayerDeathEvent e) {
    Location toSave = null;
    for (var l : activeBeacons) {
      if (!l.isChunkLoaded()) continue;
      var p = getProtectedPlayers(l);
      if (p.contains(e.getPlayer())) {
        toSave = l;
        break;
      }
    }
    if (toSave == null) return;
    e.setCancelled(true);
    var p = e.getPlayer();
    var tpLoc = toSave.toCenterLocation()
            .add(0, 2d, 0);
    p.getWorld()
            .spawnParticle(
                    Particle.FLASH,
                    p.getLocation(),
                    20,
                    1,
                    1,
                    1
            );
    p.getWorld()
            .spawnParticle(
                    Particle.TOTEM,
                    tpLoc,
                    20,
                    0.5,
                    0.5,
                    0.5
            );
    p.getWorld()
                    .playSound(
                            p.getLocation(),
                            Sound.ITEM_TOTEM_USE,
                            1,
                            1
                    );
    p.teleport(tpLoc);
    p.addPotionEffect(
            new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1)
    );
    p.addPotionEffect(
            new PotionEffect(PotionEffectType.ABSORPTION, 10 * 20, 1)
    );
    p.addPotionEffect(
            new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30 * 20, 0)
    );
    p.setHealth(2);
  }

  @EventHandler
  public void tick(ServerTickStartEvent e) {
    for (Location l : activeBeacons) {
      if (!l.isChunkLoaded()) continue;
      System.out.println(l.getBlock().getType());
      var protectedPlayers = getProtectedPlayers(l);
      for (Player p : protectedPlayers) {
        p.getWorld().spawnParticle(
                Particle.TOTEM,
                p.getLocation(),
                1,
                1,
                1,
                1,
                0.02
        );
      }
    }
  }

  private List<Player> getProtectedPlayers(Location l) {
    var mgr = GameManager.getGameManager().getPlayerManager();
    var num = blockTeams.get(l.toBlockLocation());

    return Bukkit.getOnlinePlayers().stream()
            .map(p -> (Player) p)
            .filter(p -> p.getWorld() == l.getWorld())
            .filter(p -> mgr.getOrCreateUhcPlayer(p).getTeam().getTeamNumber() == num)
            .filter(p -> p.getLocation().distance(l) <= 40)
            .toList();
  }

}
