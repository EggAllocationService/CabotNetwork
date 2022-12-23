package dev.cabotmc.velocityagent;

import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;

import dev.cabotmc.vanish.VanishManager;
import dev.cabotmc.velocityagent.queue.Queue;
import dev.cabotmc.velocityagent.queue.QueueManager;
import dev.cabotmc.velocityagent.queue.SoloHCGamemode;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

public class JoinMessageListener {
    static final int GRAY = 0x3b3b3b;
    static final int GREEN = 0x1a8011;
    static final int RED = 0x733939;
    static final int YELLOW = 0x737332;
    
    @Subscribe
    public void join(PostLoginEvent e) {
        var base = Component.text("[", TextColor.color(GRAY));
        base = base.append(Component.text("+", TextColor.color(GREEN)));
        base = base.append(Component.text("]", TextColor.color(GRAY)));
        base = base.append(Component.text(" " + e.getPlayer().getUsername(), TextColor.color(0xe0e0e0)));
        var base2 = base;
        var isVanished = VanishManager.isVanished(e.getPlayer().getUniqueId());
        VelocityAgent.getProxy().getAllPlayers().forEach(p -> {
            if (!isVanished || p.hasPermission("vanish.see")) {
                p.sendMessage(base2);
            }
        });

    
    }
    @Subscribe
    public void leave(DisconnectEvent e) {
        var base = Component.text("[", TextColor.color(GRAY));
        base = base.append(Component.text("-", TextColor.color(RED)));
        base = base.append(Component.text("]", TextColor.color(GRAY)));
        base = base.append(Component.text(" " + e.getPlayer().getUsername(), TextColor.color(0xa0a0a0)));
        var base2 = base;
        var isVanished = VanishManager.isVanished(e.getPlayer().getUniqueId());
        VelocityAgent.getProxy().getAllPlayers().forEach(p -> {
            if (!isVanished || p.hasPermission("vanish.see")) {
                p.sendMessage(base2);
            }
        });
        QueueManager.removeQueue(e.getPlayer());
    }
    
    @Subscribe
    public void change(ServerConnectedEvent e) {
        if (!e.getPreviousServer().isPresent()) return;
        var name = e.getPlayer().getUsername();
        var base = Component.text("Proxy | ", TextColor.color(GRAY));
        var leaveMsg = base.append(Component.text(name + " switched to ", TextColor.color(0xe0e0e0)));
        leaveMsg = leaveMsg.append(Component.text(e.getServer().getServerInfo().getName(), TextColor.color(YELLOW)));
        var joinMsg = base.append(Component.text(name + " joined this server from ", TextColor.color(0xa0a0a0)));
        joinMsg = joinMsg.append(Component.text(e.getPreviousServer().get().getServerInfo().getName(), TextColor.color(YELLOW)));
        var join2 = joinMsg;
        var leave2 = leaveMsg;
        var isVanished = VanishManager.isVanished(e.getPlayer().getUniqueId());
        e.getServer().getPlayersConnected().forEach(p -> {
            if (!isVanished || p.hasPermission("vanish.see")) {
                p.sendMessage(join2);
            }
        });
        e.getPreviousServer().get().getPlayersConnected().forEach(p -> {
            if (!isVanished || p.hasPermission("vanish.see")) {
                p.sendMessage(leave2);
            }
        });
    }
    @Subscribe
    public void downstreamDisconnect(ServerPostConnectEvent e) {
        if (e.getPreviousServer() != null) {
            var srvName = e.getPreviousServer().getServerInfo().getName();
            if (!SoloHCGamemode.playerCache.containsKey(srvName)) return;
            var ownerName = SoloHCGamemode.playerCache.get(srvName);
            if (ownerName.equals(e.getPlayer().getUsername())) {
                SoloHCGamemode.playerCache.remove(srvName);
            }
        }
    }
    
    @Subscribe
    public void tab(TabCompleteEvent e) {
        e.getPlayer().sendMessage(Component.text("Tab complete detected!"));   
    }
}
