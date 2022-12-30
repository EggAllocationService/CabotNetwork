package dev.cabotmc.velocityagent.chat;

import com.velocitypowered.api.proxy.Player;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import de.themoep.minedown.adventure.MineDownParser.Option;
import dev.cabotmc.velocityagent.VelocityAgent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

public class ChatFormatter {
    static Style default_font = Style.style(c -> c.font(Key.key("minecraft", "default")));

    public static Component format(Player from, String message) {
        var p = VelocityAgent.luckPerms.getUserManager().getUser(from.getUniqueId());
        var prefix = p.getCachedData().getMetaData().getPrefix();
        var suffix = p.getCachedData().getMetaData().getSuffix();
        Component base = Component.text("");
        if (prefix != null) {
            base = formatPrefix(prefix);
            base = base.append(Component.text(" " + from.getUsername(), default_font).style(s -> {
                s.color(NamedTextColor.WHITE);
            }));
        } else {
            base = Component.text(from.getUsername(), default_font).style(s -> {
                s.color(NamedTextColor.WHITE);
            });
        }
        if (suffix != null) {
            base = base.append(formatPrefix(suffix));
        }


        base = base.append(Component.text(": ", default_font.color(TextColor.color(0x38c0ff))));
        base = base.append(formatPrefix(message).style(default_font).colorIfAbsent(NamedTextColor.WHITE));
        return base;
    }
    public static Component formatPrefix(String prefix) {
        return MineDown.parse(prefix);
    }
    public static Component formatMessage(String message) {
        var parser = new MineDownParser();
        
        parser.urlDetection(true);
        return parser.parse(message).asComponent();
    }
}
