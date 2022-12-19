package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.Damageable;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import dev.cabotmc.hardcore.randomdiff.RandomDifficulty;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MissHits extends Modifier implements Listener {
    public MissHits() {
        super("Hit Your Shots");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Missing a hit with your sword damages you for a heart", TextColor.color(Modifier.BAD_COLOR))
        .append(Component.text("\nMissing a hit with your bow damages you for a heart", TextColor.color(Modifier.BAD_COLOR)))
        .append(Component.text("\nAny entity missing an arrow shot will take a heart of damage", TextColor.color(Modifier.GOOD_COLOR)))
        .append(
            Component.text("\nGain +3 to your points multiplier", TextColor.color(Modifier.GOOD_COLOR))
        ).append(
            Component.text("\nIf Instant Death is also active, double your points multiplier", TextColor.color(Modifier.GOOD_COLOR))
        );
    }

    @Override
    public void activate() {
        HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 3);
    }
    @Override
    public void postActivate() {
        var d = (RandomDifficulty) HardcorePlugin.difficulty;
        for (var x : d.finalModifiers) {
            if (x instanceof InstaDeath) {
                HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() * 2);
                return;
            }
        }
    }
    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) return;
        if (e.getPlayer().getTargetEntity(5) != null) return;
        var p = e.getPlayer();
        var i = p.getInventory().getItemInMainHand();
        if (i.getType().toString().endsWith("SWORD")) {
            p.damage(2);
        }
    }
    @EventHandler
    public void projectileHit(ProjectileHitEvent e) {
        if (e.getHitBlock() == null) return;
        if (e.getEntityType() != EntityType.ARROW && e.getEntityType() != EntityType.SPECTRAL_ARROW) return;
        var s = e.getEntity().getShooter();
        if (s instanceof LivingEntity) {
            var l = (LivingEntity) s;
            l.damage(2);
        }
    }

    
}
