package dev.cabotmc.velocityagent;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent.ServerKickResult;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.cabotmc.mgmt.ProtocolHelper;
import dev.cabotmc.mgmt.protocol.ClientIdentifyMessage;
import dev.cabotmc.mgmt.protocol.CrossServerMessage;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

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
        var meta = proxy.getCommandManager().metaBuilder("create")
            .plugin(this)
            .build();
        proxy.getCommandManager().register(meta, new CreateCommand());
        meta = proxy.getCommandManager().metaBuilder("solohc")
        .plugin(this)
        .build();
        proxy.getCommandManager().register(meta, new HCCommand());
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
                            var m = "[SERVER UP] " + msg.serverName;
                            proxy.sendMessage(Component.text(m));
                            logger.info(m);
                            proxy.registerServer(s);
                        }
                    } else {
                        // remove server if it exists
                        if (proxy.getServer(msg.serverName).isPresent()) {
                            var server = proxy.getServer(msg.serverName).get();
                            proxy.unregisterServer(server.getServerInfo());
                        }
                    }
                } else if (o instanceof CrossServerMessage) {
                    var msg = (CrossServerMessage) o;
                    if (msg.data.startsWith("hcready")) {
                        var stuff = msg.data.split(":");
                        var p = proxy.getPlayer(stuff[1]);
                        var serv = proxy.getServer(stuff[2]);
                        if (p.isEmpty() || serv.isEmpty()) return;
                        var pp = p.get();
                        if (HCCommand.waitingBars.containsKey(pp)) {
                            pp.hideBossBar(HCCommand.waitingBars.get(pp));
                            HCCommand.waitingBars.remove(pp);
                        }
                        pp.sendMessage(Component.text("Transferring you to " + stuff[2]));
                        pp.createConnectionRequest(serv.get()).fireAndForget();
                    }
                }
            }
        });
        var identifyMessage = new ClientIdentifyMessage();
        identifyMessage.kind = "velocity";
        identifyMessage.instanceName = "velocity";
        kryoClient.sendTCP(identifyMessage);
    }
    @Subscribe
    public void handleDisconnect(KickedFromServerEvent e) {
        if (e.kickedDuringServerConnect()) {
            var reason = Component.text("Transfer failed, the target server kicked you: ", TextColor.color(249, 89, 68));
            if (e.getServerKickReason().isPresent()) {
                reason = reason.append(e.getServerKickReason().get());
            } else {
                reason = reason.append(Component.text("Disconnected"));
            }
            e.setResult(KickedFromServerEvent.Notify.create(reason));

        } else {
            var reason = Component.text("Your connected server went down, transferring you to the lobby", TextColor.color(249, 89, 68));
            e.setResult(KickedFromServerEvent.RedirectPlayer.create(proxy.getServer("lobby").get(), reason));
        }
    }
    @Subscribe
    public void handleLogin(PlayerChooseInitialServerEvent e) {
        if (proxy.getServer("lobby").isPresent()) {
            e.setInitialServer(proxy.getServer("lobby").get());
        }
    }
}
