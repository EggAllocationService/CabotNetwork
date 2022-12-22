package dev.cabotmc.velocityagent.chat;

import com.velocitypowered.api.proxy.Player;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;

public class ChatFormatter {
    public static Component format(Player from, String message) {
        return formatMessage(message);
    }

    public static Component formatMessage(String message) {
        return MineDown.parse(message);
    }
}
