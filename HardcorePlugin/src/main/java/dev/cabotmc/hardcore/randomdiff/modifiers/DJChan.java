package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class DJChan extends Modifier implements Listener {
    public DJChan() {
        super("DJ Chan");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Gain wallhacks", TextColor.color(Modifier.GOOD_COLOR)).append(
            Component.text("\nGain +1 to your multiplier", TextColor.color(Modifier.GOOD_COLOR))
        ).append(
            Component.text("\nYou are banned from Five Guys", TextColor.color(Modifier.BAD_COLOR))
        );
    }

    @Override
    public void activate() {
	HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 1);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void spawn(EntitySpawnEvent e) {
        e.getEntity().setGlowing(true);
    }
    
}
