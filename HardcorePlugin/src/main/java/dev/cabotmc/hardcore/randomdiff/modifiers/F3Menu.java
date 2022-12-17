package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class F3Menu extends Modifier {
    public F3Menu() {
        super("F-Threnis");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Your F3 menu provides no useful information.", TextColor.color(Modifier.BAD_COLOR))
        .append(
            Component.text("\nGain +1.5 to your base multiplier", TextColor.color(Modifier.BAD_COLOR))
        );
    }

    @Override
    public void activate() {
       for (var w: Bukkit.getWorlds()) {
        w.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
       }
       HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 1.5);
    }
    
}