package dev.cabotmc.velocityagent.chat;

import com.velocitypowered.api.proxy.Player;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

public class ChatFormatter {
    static Style default_font = Style.style(c -> c.font(Key.key("minecraft", "default")));
    public static Component format(Player from, String message) {
        var base = Component.text(from.getUsername(), default_font).style(s -> {
            s.color(NamedTextColor.WHITE);
        });
        if (from.hasPermission("icon.administrator")) {
            var admin = Component.text("\uE006").style(s -> {
                s.font(Key.key("cabot", "icons"));
                s.color(TextColor.color(0xca0707));
            });
            admin = admin.append(Component.text(" ", default_font));
            base = admin.append(base);
            
        }
        base = base.append(Component.text(": ", default_font.color(TextColor.color(0x38c0ff))));
        base = base.append(formatMessage(message).style(default_font).colorIfAbsent(NamedTextColor.WHITE));
        return base;
    }

    public static Component formatMessage(String message) {
        return MineDown.parse(message);
    }
}
