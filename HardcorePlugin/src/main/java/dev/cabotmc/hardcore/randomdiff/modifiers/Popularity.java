package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class Popularity extends Modifier implements Listener {
    public Popularity() {
        super("Center of Attention");
    }

    @Override
    public Component generateDescription() {
        return Component.text("Mobs will only ever target you", TextColor.color(Modifier.BAD_COLOR))
                .append(Component.text("\nIron Golems and dogs are exempt", TextColor.color(Modifier.GOOD_COLOR)))
                .append(Component.text("\nGain +2 to your point multiplier", TextColor.color(Modifier.GOOD_COLOR)));
    }

    @Override
    public void activate() {
        HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void target(EntityTargetEvent e) {
        if (e.getEntityType() == EntityType.IRON_GOLEM) return;
        if (e.getEntityType() == EntityType.WOLF) {
            Wolf w = (Wolf) e.getEntity();
            if (w.isTamed()) return;
        }
        if (e.getEntity() instanceof Mob && e.getTarget() != null) {
            if (!(e.getTarget() instanceof Player)) {
                e.setCancelled(true);
            }
        }
    }
}