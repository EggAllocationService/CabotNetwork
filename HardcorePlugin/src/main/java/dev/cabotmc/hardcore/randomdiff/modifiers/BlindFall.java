package dev.cabotmc.hardcore.randomdiff.modifiers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BlindFall extends Modifier implements Listener {

    public BlindFall() {
        super("Blinding Falls");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Taking fall damage blinds and slows you", TextColor.color(Modifier.BAD_COLOR));
    }

    @Override
    public void activate() {

    }

    @EventHandler
    public void hurt(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL) {
            var d = (Player) e.getEntity();
            d.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0, true, false));
            d.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1, true, false));
        }
    }

}
