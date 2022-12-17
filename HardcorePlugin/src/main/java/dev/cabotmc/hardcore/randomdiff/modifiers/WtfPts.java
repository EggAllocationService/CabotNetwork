package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.Bukkit;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.points.PointsManager;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.hover.content.Text;

public class WtfPts extends Modifier {
    public WtfPts() {
        super("Yo Dawg, We Heard You Like Random");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Every point granted has a random multiplier", TextColor.color(Modifier.NEUTRAL_COLOR))
        .append(
            Component.text("\nMultipliers less than 1x also give you a half heart of overhealth", TextColor.color(Modifier.GOOD_COLOR))
        )
        .append(
            Component.text("\nModifiers greater than 9x will deal a heart of damage", TextColor.color(Modifier.BAD_COLOR))
        )
        .append(
            Component.text("Your base modifier is set to 1.0x", TextColor.color(Modifier.BAD_COLOR))
        );
    }
    @Override
    public void postActivate() {
        HardcorePlugin.difficulty.setMultiplier(1.0d);
    }

    @Override
    public void activate() {
       PointsManager.addMutator(c -> {
        float multiplier = (float) (Math.random() * 10);
        var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
        if (multiplier < 1) {
            p.setAbsorptionAmount(p.getAbsorptionAmount() + 1);
        } else if (multiplier > 9) {
            p.setHealth(p.getHealth() - 1);
        }
        var formatted = Math.floor(multiplier * 100) / 100;
        var msg = Component.text(Double.toString(formatted), TextColor.color(0xebe834))
            .decorate(TextDecoration.UNDERLINED)
            .decorate(TextDecoration.BOLD)
            .append(Component.text("x", TextColor.color(0x93eb34)));
        Bukkit.broadcast(Component.text(" "));
        Bukkit.broadcast(msg);
        return c * multiplier;
       });
    }
}
