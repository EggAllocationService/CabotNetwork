package dev.cabotmc.spigotagent.tickets;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class TicketListener implements Listener {
    @EventHandler
    public void use(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        var i = e.getPlayer().getInventory().getItemInMainHand();
        if (i.getItemMeta() == null) {
            return;
        }
        if (!i.getItemMeta().getPersistentDataContainer().has(TicketUtil.TICKET_KEY)) {
            return;
        }
        e.setCancelled(true);
        var type = i.getItemMeta().getPersistentDataContainer().get(TicketUtil.TICKET_KEY, PersistentDataType.BYTE);
        if (type == 1) {
            // blank ticket
            e.getPlayer().getInventory().setItemInMainHand(null);
            new TicketBrowseMenu(e.getPlayer(), true).open();
        } else if (type == 2) {
            var material = i.getItemMeta().getPersistentDataContainer().get(TicketUtil.ITEM_KEY, PersistentDataType.STRING);
            var bukkitMaterial = Material.matchMaterial(material);
            new TicketViewMenu(e.getPlayer(), bukkitMaterial).open();
        }
    }

}
