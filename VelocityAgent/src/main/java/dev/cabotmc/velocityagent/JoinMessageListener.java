package dev.cabotmc.velocityagent;

import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
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
    static byte[] RESOURCE_HASH = hexStringToByteArray( "aa0939b19949a565ae6fca6e35e4d11232164ed4");
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
        var r = VelocityAgent.getProxy().createResourcePackBuilder("https://cdn.cabotmc.dev/cabotmc_01.zip")
            .setPrompt(Component.text("You must download this pack to play"))
            .setHash(RESOURCE_HASH)
            .setShouldForce(true)
            .build();
        VelocityAgent.getProxy().getScheduler().buildTask(VelocityAgent.instance, () -> {
            e.getPlayer().sendResourcePackOffer(r);
            var spaces = Component.text("\n\n\n\n\n\n");
            e.getPlayer().sendPlayerListHeader(spaces.append(Component.text("\uE000", Style.style(b -> {
                b.font(Key.key("cabot", "icons"));
            }))).append(spaces));
        }).delay(500, TimeUnit.MILLISECONDS).schedule();
    
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
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
