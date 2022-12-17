package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.Damageable;

import dev.cabotmc.hardcore.randomdiff.Modifier;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MissHits extends Modifier implements Listener {
    public MissHits() {
        super("Hit Your Shots");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Missing a hit with your sword still damages it", TextColor.color(Modifier.BAD_COLOR));
    }

    @Override
    public void activate() {

    }

    @EventHandler
    public void swing(PlayerArmSwingEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) return;
        var p = e.getPlayer();
        var i = p.getInventory().getItemInMainHand();
        if (i.getType().toString().endsWith("SWORD")) {
            var m = (Damageable) i.getItemMeta();
            m.setDamage(m.getDamage() + 1);
            i.setItemMeta(m);
        }
    }
    @EventHandler
    public void hit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            var p = (Player) e.getDamager();
            var i = p.getInventory().getItemInMainHand();
            if (i == null || i.getType() == Material.AIR) return;
            if (i.getType().toString().endsWith("SWORD")) {
                var m = (Damageable) i.getItemMeta();
                m.setDamage(m.getDamage() - 1);
                i.setItemMeta(m);
            }
        }
    }
}
