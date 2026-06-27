package dev.cabotmc.uhc.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.configuration.PlayerConfigurationEvent;
import com.velocitypowered.api.event.player.configuration.PlayerEnteredConfigurationEvent;
import com.velocitypowered.api.event.player.configuration.PlayerFinishConfigurationEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.ChatSession;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.cabotmc.uhc.proxy.commands.SendCommand;
import dev.cabotmc.uhc.proxy.commands.ServerCommand;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import javax.sql.ConnectionEvent;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Plugin(
    id = "cabotproxy",
    name = "CabotProxy",
    version = "1.0.0",
        dependencies = {
            @Dependency(id = "luckperms", optional = false),
            @Dependency(id = "velocity-scoreboard-api", optional = false)
    }
)
public class CabotProxy {
    public static CabotProxy instance;
    @Inject private Logger logger;
    @Inject private ProxyServer server;

    private Jedis jedis;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        jedis = new Jedis("redis", 6379);
        jedis.connect();
        Thread.startVirtualThread(() -> {
            jedis.subscribe(new ProxySub(this, server, logger), "proxy", "send", "send-player");
        });
        logger.info("Redis connection initialized");

        server.getScheduler().buildTask(this, this::updateHeaders).repeat(1, TimeUnit.SECONDS).schedule();

        server.getEventManager().register(this, new TeamRewriter(server));

        server.getChannelRegistrar().register(TransitionEffects.CONNECT_CHANNEl);
        server.getCommandManager().unregister("server");
        server.getCommandManager().unregister("send");
        var fx = new TransitionEffects(server, this);

        server.getEventManager().register(this, fx);
        var sendMeta = server.getCommandManager().metaBuilder("send")
                .plugin(this)
                .build();
        server.getCommandManager().register(sendMeta, SendCommand.create(server, fx));

        var serverMeta = server.getCommandManager().metaBuilder("server")
                .plugin(this)
                .build();
        server.getCommandManager().register(serverMeta, new ServerCommand(server, fx));
    }

    private static final Component HEADER_LOGO = Component.text("\n\n\n\n\uEB01\n\n\n\n", TextColor.color(0xFF0000)).shadowColor(ShadowColor.none());
    private final Set<UUID> toNotify = new HashSet<>();
    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        toNotify.add(event.getPlayer().getUniqueId());
        event.getPlayer().sendPlayerListFooter(Component.text("\n\n\n\na\n\n\n\n").font(Key.key("cabot", "icons")));

        server.sendMessage(MiniMessage.miniMessage().deserialize("<green>+<grey> " + getPlayerNameFormatString(event.getPlayer(), "<gray>")));

    }

    static final UUID RESOURCES_UUID = UUID.fromString("e10f6290-048a-4e00-b287-2c02cdb072f9");
    static final UUID UHC_UUID = UUID.fromString("2c2d123e-e343-432b-a045-0af679656346");
    final Map<UUID, CompletableFuture<Boolean>> resourcePackFutures = new HashMap<>();
    @Subscribe
    public void pack(PlayerResourcePackStatusEvent event) {
        if (resourcePackFutures.containsKey(event.getPlayer().getUniqueId())) {
            var future = resourcePackFutures.get(event.getPlayer().getUniqueId());
            var status = event.getStatus();
            if (status.isIntermediate()) return;

            future.complete(
                    status == PlayerResourcePackStatusEvent.Status.SUCCESSFUL
            );
        }
    }

    @Subscribe
    public void configure(PlayerConfigurationEvent event) {
        if (!event.player().getAppliedResourcePacks().isEmpty()) {
            return;
        }

        event.player().sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                        .packs(
                                ResourcePackInfo.resourcePackInfo(RESOURCES_UUID, URI.create("https://objects.cabotmc.dev/26_3.zip"), "70188c8e424f75bf41832d6e252f7a1c643b3bd0"),
                                ResourcePackInfo.resourcePackInfo(UHC_UUID, URI.create("https://objects.cabotmc.dev/cabotuhc_26.2_v4.zip"), "a085d41ae7db3d90fe15ce4ae2d6ae8f39ae3904")
                        )
                        .prompt(Component.text("Required Cabot resources"))
                .required(true)
                .build());

        var future = new CompletableFuture<Boolean>();
        resourcePackFutures.put(event.player().getUniqueId(), future);
        try {
            if (!future.get()) {
                event.player().disconnect(Component.text("Failed to download required resources"));
            }
        } catch (InterruptedException | ExecutionException e) {
            // pass
            e.printStackTrace();
        } finally {
            resourcePackFutures.remove(event.player().getUniqueId());
        }
    }

    @Subscribe
    public void onJoin(ServerPostConnectEvent event) {
        if (toNotify.contains(event.getPlayer().getUniqueId())) {
            toNotify.remove(event.getPlayer().getUniqueId());

            // determine how the player is connected
            var ip = event.getPlayer().getRemoteAddress().getHostString();
            if (ip.equals("127.0.0.1") || ip.startsWith("192.168.")) {
                event.getPlayer().sendMessage(
                    formatProxyMessage("<rainbow>Connected via local network. Hello Kyle :)")
                );
            } else if (ip.equals("100.67.67.1")) {
                // from the-server
                event.getPlayer().sendMessage(
                        formatProxyMessage("<red>Your game traffic is being proxied via mtl-1. This will lead to significantly worse latency and stability.")
                );
            } else if (ip.startsWith("100.67.67")) {
                event.getPlayer().sendMessage(
                        formatProxyMessage("<#2092a6>Your game traffic is being routed optimally via Netbird")
                );
            }
        }
        var curServer = event.getPlayer().getCurrentServer().get().getServer();
        server.getScheduler().buildTask(this, () -> {
                    rebuildPlayerListForServer(curServer);
                })
                .delay(1000, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Subscribe
    public void onLeave(DisconnectEvent e) {
        var s = e.getPlayer().getCurrentServer().get().getServer();
        server.getScheduler().buildTask(this, () -> {
            rebuildPlayerListForServer(s);
        }).delay(10, TimeUnit.MILLISECONDS).schedule();
        server.sendMessage(MiniMessage.miniMessage().deserialize("<red>-<grey> " + getPlayerNameFormatString(e.getPlayer(), "<gray>")));

        if (resourcePackFutures.containsKey(e.getPlayer().getUniqueId())) {
            var future = resourcePackFutures.get(e.getPlayer().getUniqueId());
            future.complete(false);
            resourcePackFutures.remove(e.getPlayer().getUniqueId());
        }
    }

    @Subscribe
    public void chat(PlayerChatEvent e) {
        e.setResult(PlayerChatEvent.ChatResult.denied());

        var message = formatChatMessage(e.getPlayer(), e.getMessage());
        for (var player : server.getAllPlayers()) {
            player.sendMessage(message);
        }
    }

    private Component formatChatMessage(Player sender, String message) {
        return formatPlayerName(sender).append(MiniMessage.miniMessage().deserialize("<reset><#04d7de>:<reset> " + message));
    }

    private void rebuildPlayerListForServer(RegisteredServer target) {
        logger.info("Rebuilding player list for " + target.getServerInfo().getName());
        var players = target.getPlayersConnected();

        for (var player : players) {
            player.getTabList().getEntries().forEach(e -> {
                var id = e.getProfile().getId();
                if (server.getPlayer(id).isEmpty()) return;
                var p = server.getPlayer(id).get();

                e.setDisplayName(formatPlayerName(p));
                e.setListOrder(getPlayerSortOrder(p));
            });
        }
    }

    private void updateHeaders() {
        var mm = MiniMessage.miniMessage();
        for (var player : server.getAllPlayers()) {
            var ping = player.getPing();
            var server = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer().getServerInfo().getName() : "Unknown";
            var connectionMethod = "Unknown";
            var ip = player.getRemoteAddress().getHostString();

            if (ip.equals("127.0.0.1") || ip.startsWith("192.168.")) {
                connectionMethod = "<green>Local";
            } else if (ip.equals("100.67.67.1")) {
                // from the-server
                connectionMethod = "<red>Proxy";
            } else if (ip.startsWith("100.67.67")) {
                connectionMethod = "<green>Netbird";
            }

            player.sendPlayerListHeaderAndFooter(
                    HEADER_LOGO,
                    mm.deserialize("\n <#f505f1>Connected to <#05f515>" + server + " <reset>|<#04d7de> Routed via " + connectionMethod + " <#04d7de>" + ping + "ms \n")
            );
        }
    }

    private Component formatPlayerName(Player player) {
        return formatPlayerName(player, "<reset>");
    }
    private Component formatPlayerName(Player player, String nameFormatting) {

        return MiniMessage.miniMessage().deserialize("<reset>" + getPlayerNameFormatString(player, nameFormatting));
    }

    private String getPlayerNameFormatString(Player player, String prepend) {
        var api = LuckPermsProvider.get();
        var user = api.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData();
        var prefix = user.getPrefix();
        if (prefix == null) return prepend + player.getUsername();

        return prefix + "<reset> " + prepend + player.getUsername();
    }

    private int getPlayerSortOrder(Player player) {
        var api = LuckPermsProvider.get();
        var user = api.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData();
        var w = user.getMetaValue("cabot.weight");
        if (w == null) return 0;
        try {
            return Integer.parseInt(w);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Component formatProxyMessage(String message) {
        var prefix = Component.text("Harbourmaster | ", TextColor.color(0x406060)).append(Component.text("", NamedTextColor.GRAY));

        return prefix.append(MiniMessage.miniMessage().deserialize(message));
    }
}
