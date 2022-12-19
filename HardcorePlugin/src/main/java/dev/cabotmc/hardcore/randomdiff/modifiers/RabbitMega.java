package dev.cabotmc.hardcore.randomdiff.modifiers;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.ExperienceOrb.SpawnReason;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class RabbitMega extends Modifier implements Listener {
    public RabbitMega() {
        super("Rabid Rabbits");
    }
    @Override
    public Component generateDescription() {
        return Component.text("20% of all mobs are replaced by juiced-up killer rabbits.", TextColor.color(Modifier.BAD_COLOR)).append(
            Component.text("\nGain +2 to your multiplier", TextColor.color(Modifier.GOOD_COLOR))
        );
    }

    @Override
    public void activate() {
	HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 2);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void spawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.RABBIT || !(e.getEntity() instanceof LivingEntity)) return;
        var v = ((LivingEntity) e.getEntity()).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (v == null) return;
        if (Math.random() > 0.80) {
            e.setCancelled(true);
            var r = (Rabbit) e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.RABBIT);
            r.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
            r.setGlowing(true);
            HardcorePlugin.MINIBOSS_TEAM.addEntities(r);
            r.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000000 * 20, 1, false, true));
            r.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(64);
            r.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(16);
        }
    }
    
}
