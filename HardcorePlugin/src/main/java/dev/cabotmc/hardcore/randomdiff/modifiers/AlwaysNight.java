package dev.cabotmc.hardcore.randomdiff.modifiers;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;

public class AlwaysNight extends Modifier {
    public AlwaysNight() {
        super("Night Owl");
    }

    public Component generateDescription() {
        return Component.text("It is always night.", TextColor.color(Modifier.BAD_COLOR))
                .append(Component.text("\n+2 to your modifier", TextColor.color(Modifier.GOOD_COLOR)));
    }

    public void activate() {
        Bukkit.getWorld("world").setTime(18000L);
        Bukkit.getWorld("world").setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 2);
        Bukkit.getServer().getPluginManager().registerEvents(new BedExplodeListener(), HardcorePlugin.instance);
    }
}