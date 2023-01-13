package dev.cabotmc.velocityagent.chat;

import com.velocitypowered.api.command.CommandSource;
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
import net.kyori.adventure.text.format.TextDecoration;

public class ChatFormatter {
    static Style default_font = Style.style(c -> c.font(Key.key("minecraft", "default")));

    public static Component format(Player from, String message) {
        
        Component base = formatUsername(from);

        base = base.append(Component.text(": ", default_font.color(TextColor.color(0x38c0ff))));
        base = base.append(formatPrefix(message).style(default_font).colorIfAbsent(NamedTextColor.WHITE));
        return base;
    }
    public static Component formatPrefix(String prefix) {
        return MineDown.parse(prefix);
    }
    public static Component formatUsername(Player from) {
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
        return base;
    }
    public static Component formatMessage(String message) {
        var parser = new MineDownParser();
        
        parser.urlDetection(true);
        return parser.parse(message).asComponent();
    }
    public static final TextColor DM_COLOR = TextColor.color(0xe0923f);
    public static Component blockedBase = Component.text("<blocked dm>", TextColor.color(0x753030)).decorate(TextDecoration.ITALIC);
    public static Component[] formatDM(String message, Player sender, Player target, boolean senderBlocked) {

        var msg = formatPrefix(message).style(default_font).colorIfAbsent(NamedTextColor.WHITE);
        // create message to send to sender,target
        var results = new Component[2];

        // toSender
        if (senderBlocked) {
            var infoMsg = Component.text(target.getUsername(), TextColor.color(0x753030))
                .hoverEvent(Component.text("This person has blocked you and will not see your messages", TextColor.color(0x753030)));
            var senderPrefix = Component.text("[you → ", DM_COLOR);
            senderPrefix = senderPrefix.append(infoMsg);
            senderPrefix = senderPrefix.append(Component.text("]: ", DM_COLOR));
            results[0] = senderPrefix.append(msg);
        } else {
            var senderPrefix = Component.text("[you → " + target.getUsername() + "]: ", DM_COLOR);
            results[0] = senderPrefix.append(msg);
        }
        

        // to reciever
        var recieverPrefix = Component.text("[" + sender.getUsername() + " → you]: ", DM_COLOR); 
        results[1] = recieverPrefix.append(msg);
        if (senderBlocked) {
            results[1] = blockedBase.hoverEvent(results[1]);
        }
        return results;
    }
}
