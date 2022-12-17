package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.Difficulty;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class DoublePoints extends Modifier {
    public DoublePoints() {
        super("Double Trouble");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Your score multiplier is doubled, but the difficulty is set to hard.", TextColor.color(Modifier.NEUTRAL_COLOR));
    }

    @Override
    public void activate() {
        
        HardcorePlugin.difficulty.setting = Difficulty.HARD;
    }
    @Override
    public void postActivate() {
        HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() * 2);
    }
    
}
