package dev.cabotmc.uhc.proxy;

import com.velocitypowered.api.proxy.ProxyServer;
import redis.clients.jedis.JedisPubSub;

public class ProxySub extends JedisPubSub {
    private Object plugin;
    private ProxyServer server;

    public ProxySub(Object plugin, ProxyServer server) {
        this.server = server;
        this.plugin = plugin;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("send")) {
            var target = server.getServer(message);
            if (target.isEmpty()) return;
            var real = target.get();

            server.getScheduler().buildTask(plugin, () -> {
                server.getAllPlayers().forEach(player -> {
                    player.createConnectionRequest(real).fireAndForget();
                });
            }).schedule();
        }
    }
}
