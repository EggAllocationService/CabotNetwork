package dev.cabotmc.uhc.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.configuration.PlayerFinishConfigurationEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Plugin(
    id = "cabotproxy",
    name = "CabotProxy",
    version = "1.0.0"
)
public class CabotProxy {

    @Inject private Logger logger;
    @Inject private ProxyServer server;

    private Jedis jedis;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        jedis = new Jedis("redis", 6379);
        jedis.connect();
        Thread.startVirtualThread(() -> {
            jedis.subscribe(new ProxySub(this, server, logger), "proxy", "send", "send-player");
        });
        logger.info("Redis connection initialized");
    }

    private final Set<UUID> toNotify = new HashSet<>();
    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        toNotify.add(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onJoin(ServerPostConnectEvent event) {
        if (toNotify.contains(event.getPlayer().getUniqueId())) {
            toNotify.remove(event.getPlayer().getUniqueId());

            // determine how the player is connected
            var ip = event.getPlayer().getRemoteAddress().getHostString();
            if (ip.equals("127.0.0.1") || ip.startsWith("192.168.")) {
                event.getPlayer().sendMessage(
                    formatProxyMessage("Connected via local network. Hello Kyle :)")
                );
            } else if (ip.equals("100.67.67.1")) {
                // from the-server
                event.getPlayer().sendMessage(
                        formatProxyMessage("<red>Your game traffic is being proxied via mtl-1. This will lead to significantly worse latency and stability.")
                );
            } else if (ip.startsWith("100.67.67")) {
                event.getPlayer().sendMessage(
                        formatProxyMessage("Your game traffic is being routed optimally via Netbird")
                );
            }
        }
    }

    private Component formatProxyMessage(String message) {
        var prefix = Component.text("(Harbourmaster) ", TextColor.color(0x404040)).append(Component.text("", NamedTextColor.GRAY));

        return prefix.append(MiniMessage.miniMessage().deserialize(message));
    }
}
