package dev.cabotmc.uhc.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

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
}
