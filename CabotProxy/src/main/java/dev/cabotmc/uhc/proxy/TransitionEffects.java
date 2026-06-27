package dev.cabotmc.uhc.proxy;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;

import java.util.concurrent.TimeUnit;

public class TransitionEffects {
    public static final ChannelIdentifier CONNECT_CHANNEl = MinecraftChannelIdentifier.from("cabot:connect");
    private final ProxyServer server;
    private final CabotProxy plugin;
    public TransitionEffects(ProxyServer server, CabotProxy plugin) {
        this.server = server;
        this.plugin = plugin;
    }

    private static Component IN_ANIM = Component.text("\uEB01").shadowColor(ShadowColor.none());
    private static Component OUT_ANIM = Component.text("\uEB01", TextColor.color(0x0000FF)).shadowColor(ShadowColor.none());
    public void transferPlayer(Player p, RegisteredServer target) {
        var cur = p.getCurrentServer().orElse(null);
        if (cur != null && cur.getServer().equals(target)) {
            return;
        }

        p.showTitle(
                Title.title(
                        IN_ANIM,
                        Component.empty(),
                        20, 60 * 60 * 20, 0
                )
        );


        server.getScheduler()
                .buildTask(
                        plugin,
                        () -> {
                            p.createConnectionRequest(target)
                                    .connect().thenAccept(result -> {
                                p.showTitle(Title.title(OUT_ANIM, Component.empty(), 0, 0, 20));
                            });
                        }
                )
                .delay(1, TimeUnit.SECONDS)
                .schedule();
    }

    @Subscribe
    public void onMessageFromBackend(PluginMessageEvent e) {
        if (!CONNECT_CHANNEl.equals(e.getIdentifier()) ) return;
        e.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(e.getSource() instanceof ServerConnection)) return;


        var in = ByteStreams.newDataInput(e.getData());

        var target = in.readUTF();

        var targetServer = server.getServer(target).orElse(null);
        if (targetServer == null) return;
        var p = (Player) e.getSource();
        transferPlayer(p, targetServer);
    }
}
