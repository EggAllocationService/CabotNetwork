package dev.cabotmc.hardcore.randomdiff;

import dev.cabotmc.hardcore.HardcorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PrintDiffModifier extends Modifier {

    public PrintDiffModifier() {
        super("PrintDiff");
    }

    @Override
    public Component generateDescription() {
        var dif = HardcorePlugin.difficulty.setting;
        return Component.text("Vanilla difficulty is set to ", TextColor.color(Modifier.GOOD_COLOR))
            .append(Component.text(dif.toString().toLowerCase(), TextColor.color(Modifier.NEUTRAL_COLOR)));
    }

    @Override
    public void activate() {
        
    }
    @Override
    public Component generateInfo() {
        return generateDescription();
    }
    
}
