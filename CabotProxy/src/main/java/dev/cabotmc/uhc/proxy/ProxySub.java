package dev.cabotmc.uhc.proxy;

import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPubSub;

public class ProxySub extends JedisPubSub {
    private Object plugin;
    private ProxyServer server;
    private Logger logger;

    public ProxySub(Object plugin, ProxyServer server, Logger logger) {
        this.server = server;
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("[" + channel + "] Message received: " + message);
        if (channel.equals("send")) {
            var target = server.getServer(message);
            if (target.isEmpty()) return;
            var real = target.get();

            server.getScheduler().buildTask(plugin, () -> {
                server.getAllPlayers().forEach(player -> {
                    player.createConnectionRequest(real).fireAndForget();
                });
            }).schedule();
        } else if (channel.equals("send-player")) {
            var args = message.split(",");

            var target = server.getServer(args[0]);
            if (target.isEmpty()) {
                logger.info("No server found for " + args[0]);
                return;
            }
            var player = server.getPlayer(args[1]);
            if (player.isEmpty()) {
                logger.info("No player found for " + args[1]);
                return;
            }
            server.getScheduler().buildTask(plugin, () -> {
                player.get().createConnectionRequest(target.get()).fireAndForget();
            }).schedule();
        }
    }
}
