package dev.cabotmc.velocityagent;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing.SamplePlayer;

import dev.cabotmc.mgmt.ProtocolHelper;
import dev.cabotmc.mgmt.protocol.ClientIdentifyMessage;
import dev.cabotmc.mgmt.protocol.CrossServerMessage;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;
import dev.cabotmc.vanish.VanishManager;
import dev.cabotmc.velocityagent.chat.BlockCommand;
import dev.cabotmc.velocityagent.chat.ChatListener;
import dev.cabotmc.velocityagent.chat.MessageCommand;
import dev.cabotmc.velocityagent.chat.NewChatPacket;
import dev.cabotmc.velocityagent.db.Database;
import dev.cabotmc.velocityagent.queue.QueueManager;
import dev.cabotmc.velocityagent.resourcepack.PackManager;
import dev.cabotmc.velocityagent.santahat.SanataManager;
import dev.cabotmc.velocityagent.santahat.SantaThread;
import dev.cabotmc.velocityagent.vanish.VanishCommand;
import dev.cabotmc.velocityagent.vanish.VanishMessageJob;
import dev.simplix.protocolize.api.PacketDirection;
import dev.simplix.protocolize.api.Protocol;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.mapping.AbstractProtocolMapping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import org.slf4j.Logger;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "velocityagent", name = "VelocityAgent", version = "1.0.0", authors = {
        "EggAllocationService" }, dependencies = {@Dependency(id = "protocolize"), @Dependency(id = "luckperms")})
public class VelocityAgent {
    public static Client kryoClient;
    public static SantaThread thread;
    public static VelocityAgent instance;
    public static LuckPerms luckPerms;
    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxy;

    private static ProxyServer proxyServer;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException {
        kryoClient = new Client();
        instance = this;
        thread = new SantaThread();
        luckPerms = LuckPermsProvider.get();
        thread.start();
        kryoClient.start();
        proxyServer = proxy;
        ProtocolHelper.registerClasses(kryoClient.getKryo());
        kryoClient.connect(5000, "172.17.0.1", 3269);
        proxy.getEventManager().register(this, new JoinMessageListener());
        proxy.getEventManager().register(this, new PackManager());
        proxy.getEventManager().register(this, new ChatListener());
        logger.info("Connected to management server");
        
        var meta = proxy.getCommandManager().metaBuilder("create")
                .plugin(this)
                .build();
        proxy.getCommandManager().register(meta, new CreateCommand());
        meta = proxy.getCommandManager().metaBuilder("games")
                .plugin(this)
                .aliases("play", "g")
                .build();
        proxy.getCommandManager().register(meta, new GamesMenuCommand());
        meta = proxy.getCommandManager().metaBuilder("servers")
                .plugin(this)
                .aliases("s", "srv")
                .build();
        proxy.getCommandManager().register(meta, new ServersCommand());
        meta = proxy.getCommandManager().metaBuilder("lobby")
                .plugin(this)
                .build();
        proxy.getCommandManager().register(meta, new LobbyCommand());
        meta = proxy.getCommandManager().metaBuilder("v")
                .aliases("vanish")
                .plugin(this)
                .build();
        proxy.getCommandManager().register(meta, new VanishCommand());
        meta = proxy.getCommandManager().metaBuilder("block")
                .plugin(this)
                .build();
        proxy.getCommandManager().register(meta, BlockCommand.create());
        meta = proxy.getCommandManager().metaBuilder("msg")
            .aliases("w", "whisper")
            .plugin(this)
        .build();
        proxy.getCommandManager().register(meta, MessageCommand.create());
        VanishMessageJob.generateThings();
        proxy.getScheduler().buildTask(this, VanishMessageJob::runJob).repeat(50, TimeUnit.MILLISECONDS).schedule();
        proxy.getScheduler().buildTask(this, () -> {
            var separator = Component.text(" - ", TextColor.color(0x3c3c3c));
            var first = Component.text("\n  " + proxy.getPlayerCount() + " players online", TextColor.color(0x4287f5));
            proxy.getAllPlayers().forEach(p -> {
                var base = first.append(separator);
                base = base.append(Component.text("Playing on ", TextColor.color(0x4287f5)));
                if (p.getCurrentServer().isPresent()) {
                    base = base.append(Component.text(p.getCurrentServer().get().getServerInfo().getName(), TextColor.color(0xf542da)));
                    base = base.append(separator);
                }
                var ping = p.getPing();
                TextColor color;
                if (ping < 100) {
                    color = TextColor.color(0x18fa66);
                } else if (ping < 250) {
                    color = TextColor.color(0xa9118);
                } else {
                    color = TextColor.color(0xd11925);
                }
                base = base.append(Component.text("Ping: ", TextColor.color(0x4287f5)));
                base = base.append(Component.text(Long.toString(ping), TextColor.color(color)));
                base = base.append(Component.text("ms  ", TextColor.color(0x4287f5)));
                p.sendPlayerListFooter(base);
            });
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }

    public static ProxyServer getProxy() {
        return proxyServer;
    }

    @Subscribe
    public void onProxyReady(ListenerBoundEvent e) {
        Database.init();
        VanishManager.init(Database.vanish);
        var mappings = Arrays.asList(AbstractProtocolMapping.rangedIdMapping(760, 760, 0x33));
        Protocolize.protocolRegistration().registerPacket(mappings, Protocol.PLAY, PacketDirection.CLIENTBOUND, NewChatPacket.class);
        kryoClient.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof ServerStatusChangeMessage) {
                    var msg = ((ServerStatusChangeMessage) o);
                    logger.info("Server status changed: " + msg.serverName + " is now online=" + msg.online);
                    if (msg.online) {
                        // add server if it doesnt exist already
                        if (proxy.getServer(msg.serverName).isEmpty()) {
                            var s = new ServerInfo(msg.serverName,
                                    new InetSocketAddress(msg.connectAddress, msg.connectPort));
                            var m = "[SERVER UP] " + msg.serverName;
                            for (var P : proxy.getAllPlayers()) {
                                if (P.hasPermission("cloud.servernotify")) {
                                    P.sendMessage(Component.text(m));
                                }
                            }
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
                    logger.info("cross server message from " + msg.from + ": " + msg.data);
                    if (msg.data.startsWith("token")) {
                        var stuff = msg.data.split(":");
                        QueueManager.serverReady(msg.from, stuff[1]);
                    } else if (msg.data.startsWith("queue")) {
                        QueueManager.serverMessage(msg.data, msg.from);
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
    public void replaceSkins(GameProfileRequestEvent e) {
        //SanataManager.findReplacement(e);
    }

    @Subscribe
    public void handleDisconnect(KickedFromServerEvent e) {
        if (e.kickedDuringServerConnect()) {
            var reason = Component.text("Transfer failed, the target server kicked you: ",
                    TextColor.color(249, 89, 68));
            if (e.getServerKickReason().isPresent()) {
                reason = reason.append(e.getServerKickReason().get());
            } else {
                reason = reason.append(Component.text("Disconnected"));
            }
            e.setResult(KickedFromServerEvent.Notify.create(reason));

        } else {
            var reason = Component.text("Your connected server went down, transferring you to the lobby",
                    TextColor.color(249, 89, 68));
            e.setResult(KickedFromServerEvent.RedirectPlayer.create(proxy.getServer("lobby").get(), reason));
        }
    }
    public static SamplePlayer[] players = new SamplePlayer[] {
        new SamplePlayer("DJ Chan", UUID.randomUUID()),
        new SamplePlayer("CSGO Cheat Vendor", UUID.randomUUID()),
        new SamplePlayer("Five Guys Map Creator", UUID.randomUUID())
    };
    @Subscribe
    public void serverPing(ProxyPingEvent e) {
        var t = e.getPing().asBuilder();
        
        e.setPing(t.samplePlayers(players)
        .maximumPlayers(Integer.MAX_VALUE).build());
    }

    @Subscribe
    public void handleLogin(PlayerChooseInitialServerEvent e) {
        if (SanataManager.sendToLimbo.contains(e.getPlayer().getUniqueId())) {
            // send to limbo
            SanataManager.sendToLimbo.remove(e.getPlayer().getUniqueId());
            e.setInitialServer(proxy.getServer("limbo").get());
            return;
        }
        if (e.getPlayer().getVirtualHost().isPresent()) {
            try {
                var targetServer = e.getPlayer().getVirtualHost().get().getHostName().split(".")[0];
                if (getProxy().getServer(targetServer).isPresent()) {
                    e.setInitialServer(getProxy().getServer(targetServer).get());
                    return;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (proxy.getServer("lobby").isPresent()) {
            e.setInitialServer(proxy.getServer("lobby").get());
        }
    }

    @Subscribe
    public void preLogin(PreLoginEvent e) {
        if (e.getConnection().getProtocolVersion() != ProtocolVersion.getProtocolVersion(765)) {
            e.setResult(PreLoginComponentResult
                    .denied(Component.text("You are not using 1.20.3!", TextColor.color(0xEa0707))));
        }
    }
}
