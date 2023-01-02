package dev.cabotmc.velocityagent.chat;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import dev.simplix.protocolize.api.Protocolize;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ChatListener {
    static Component base = Component.text("<blocked message>", TextColor.color(0x753030)).decorate(TextDecoration.ITALIC);
    @Subscribe(order = PostOrder.LAST)
    public void chat(PlayerChatEvent e) {
        if (e.getResult() == ChatResult.denied()) return;
        e.setResult(ChatResult.denied());
        var formatted = ChatFormatter.format(e.getPlayer(), e.getMessage());
        for (var p : e.getPlayer().getCurrentServer().get().getServer().getPlayersConnected()) {
            if (!BlockCommand.isBlocked(e.getPlayer().getUniqueId(), p.getUniqueId()) || e.getPlayer().hasPermission("block.bypass")) {
                p.sendMessage(formatted);
            } else {
                var redacted = base.hoverEvent(HoverEvent.showText(formatted));
                p.sendMessage(redacted);
            }
        }
       
        
        
    }
}
