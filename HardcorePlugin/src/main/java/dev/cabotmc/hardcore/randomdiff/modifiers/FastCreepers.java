package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class FastCreepers extends Modifier implements Listener {
    public FastCreepers() {
        super("Speedy Creepers");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Creepers explode quickly", TextColor.color(Modifier.BAD_COLOR));
    }

    @Override
    public void activate() {
        
        
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void spawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.CREEPER) {
            Creeper c = (Creeper) e.getEntity();
            c.setMaxFuseTicks(15);
        }
    }
    
}
