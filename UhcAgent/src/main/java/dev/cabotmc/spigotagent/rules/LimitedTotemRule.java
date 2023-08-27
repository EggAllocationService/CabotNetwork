package dev.cabotmc.spigotagent.rules;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

public class LimitedTotemRule implements Listener {
  @EventHandler
  public void pickup(PlayerAttemptPickupItemEvent e) {
    if (e.getItem().getItemStack().getType() != Material.TOTEM_OF_UNDYING) return;
    var p = e.getPlayer();
    var inInventory = p.getInventory()
            .all(Material.TOTEM_OF_UNDYING)
            .values()
            .size();

    for (var i : p.getInventory().getExtraContents()) {
      if (i != null && i.getType() == Material.TOTEM_OF_UNDYING) {
        inInventory++;
      }
    }
    var heldSlot = p.getInventory().getHeldItemSlot();
    var heldItem = p.getInventory().getItem(heldSlot);
    if (heldItem != null && heldItem.getType() == Material.TOTEM_OF_UNDYING) {
      inInventory += 1;
    }
    if (inInventory >= 2) {
      e.setCancelled(true);
    }
  }
}
