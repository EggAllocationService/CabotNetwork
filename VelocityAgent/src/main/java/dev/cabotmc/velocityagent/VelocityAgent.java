package dev.cabotmc.velocityagent;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.cabotmc.mgmt.ProtocolHelper;
import dev.cabotmc.mgmt.protocol.ClientIdentifyMessage;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

@Plugin(
        id = "velocityagent",
        name = "VelocityAgent",
        version = "1.0.0",
        authors = {"EggAllocationService"}
)
public class VelocityAgent {
    public static Client kryoClient;
    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxy;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException {
        kryoClient = new Client();
        kryoClient.start();

        ProtocolHelper.registerClasses(kryoClient.getKryo());
        kryoClient.connect(5000, "172.17.0.1", 3269);

        logger.info("Connected to management server");
    }
    @Subscribe
    public void onProxyReady(ListenerBoundEvent e) {
        kryoClient.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof ServerStatusChangeMessage) {
                    var msg = ((ServerStatusChangeMessage) o);
                    logger.info("Server status changed: " + msg.serverName + " is now online=" + msg.online);
                    if (msg.online) {
                        // add server if it doesnt exist already
                        if (proxy.getServer(msg.serverName).isEmpty()) {
                            var s = new ServerInfo(msg.serverName, new InetSocketAddress(msg.connectAddress, msg.connectPort));
                            proxy.sendMessage(Component.text("[SERVER UP] " + msg.serverName));
                        }
                    } else {
                        // remove server if it exists
                        if (proxy.getServer(msg.serverName).isPresent()) {
                            var server = proxy.getServer(msg.serverName).get();
                            proxy.unregisterServer(server.getServerInfo());
                        }
                    }
                }
            }
        });
        var identifyMessage = new ClientIdentifyMessage();
        identifyMessage.kind = "velocity";
        identifyMessage.instanceName = "velocity";
        kryoClient.sendTCP(identifyMessage);
    }
}
