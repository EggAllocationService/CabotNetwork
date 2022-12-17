package dev.cabotmc.hardcore.randomdiff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public abstract class Modifier {
    String displayName;
    public Modifier(String name) {
        displayName = name;
    }
    public static final int GOOD_COLOR = 0x28e028;
    public static final int BAD_COLOR = 0xe31612;
    public static final int NEUTRAL_COLOR = 0xe8d31a;
    public abstract Component generateDescription();
    public abstract void activate();
    public void postActivate() {
        
    }
    public Component generateInfo() {
        var c = generateDescription();
        var x = c.color();
        return Component.text(displayName).color(x)
            .hoverEvent(HoverEvent.showText(c));
    }
}
