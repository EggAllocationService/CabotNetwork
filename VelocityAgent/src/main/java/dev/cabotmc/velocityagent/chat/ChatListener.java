package dev.cabotmc.velocityagent.chat;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;

public class ChatListener {
    @Subscribe
    public void chat(PlayerChatEvent e) {
        e.setResult(ChatResult.denied());
        var formatted = ChatFormatter.format(e.getPlayer(), e.getMessage());
        e.getPlayer().getCurrentServer().get().getServer().sendMessage(formatted);
    }
}
