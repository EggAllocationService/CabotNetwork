package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class XPHealth extends Modifier implements Listener {
    public XPHealth() {
        super("Healthy Experience");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Picking up XP orbs grants overhealth", TextColor.color(Modifier.GOOD_COLOR));
    }

    @Override
    public void activate() {
        
    }
    @EventHandler
    public void pickup(PlayerPickupExperienceEvent e) {
        var p = e.getPlayer();
        p.setAbsorptionAmount(p.getAbsorptionAmount() + 1);
    }
    
}
