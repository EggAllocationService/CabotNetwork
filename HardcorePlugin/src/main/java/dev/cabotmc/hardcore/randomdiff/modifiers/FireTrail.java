package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.Damageable;

import dev.cabotmc.hardcore.randomdiff.Modifier;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class FireTrail extends Modifier implements Listener {
    public FireTrail() {
        super("Reverse Firefighter");
    }
    @Override
    public Component generateDescription() {
        return Component.text("While on fire, you leave a trail of fire behind you", TextColor.color(Modifier.BAD_COLOR));
    }

    @Override
    public void activate() {

    }

    @EventHandler
    public void move(PlayerMoveEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.SURVIVAL || e.getPlayer().getFireTicks() <= 0) return;
        var b = e.getFrom().getBlock().getType();
        if (b.toString().endsWith("AIR")) {
            e.getFrom().getWorld().setBlockData(e.getFrom(), Material.FIRE.createBlockData());
        }
    }

}