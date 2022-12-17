package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class InstaDeath extends Modifier implements Listener{
    public InstaDeath() {
        super("Sudden Death");
    }
    @Override
    public Component generateDescription() {
        return Component.text("All damage given will one shot the victim, including you. Gain a +5x points multiplier.", TextColor.color(Modifier.BAD_COLOR));
    }

    @Override
    public void activate() {
        HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 5);
    }
    @EventHandler
    public void damage(EntityDamageEvent e) {
        e.setDamage(10000d);
    }
}
