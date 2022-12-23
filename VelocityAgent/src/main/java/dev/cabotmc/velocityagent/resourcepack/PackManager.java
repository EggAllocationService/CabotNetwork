package dev.cabotmc.velocityagent.resourcepack;

import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent.Status;

import dev.cabotmc.velocityagent.VelocityAgent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;

public class PackManager {

    static byte[] RESOURCE_HASH = hexStringToByteArray("a994c10eff529e6c8b20828e97152e19bb841c9d");
    static String ICON_PACK_URL = "https://cdn.cabotmc.dev/cabotmc_02.zip";
    @Subscribe
    public void login(PostLoginEvent e) {
       
        VelocityAgent.getProxy().getScheduler().buildTask(VelocityAgent.instance, () -> {
            
            var spaces = Component.text("\n\n\n\n");
            e.getPlayer().sendPlayerListHeader(spaces.append(Component.text("\uE000", Style.style(b -> {
                b.font(Key.key("cabot", "icons"));
            }))).append(spaces));
        }).delay(500, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe
    public void change(ServerConnectedEvent e) {
        if (!e.getPlayer().getAppliedResourcePack().getUrl().endsWith("cabotmc_02.zip")
                && e.getServer().getServerInfo().getName().equals("lobby")) {
            var r = VelocityAgent.getProxy().createResourcePackBuilder(ICON_PACK_URL)
                    .setPrompt(Component.text("You must download this pack to play"))
                    .setHash(RESOURCE_HASH)
                    .setShouldForce(true)
                    .build();
            e.getPlayer().sendResourcePackOffer(r);
        }
    }
    @Subscribe
    public void status(PlayerResourcePackStatusEvent e) {
        if (e.getStatus() == Status.FAILED_DOWNLOAD || e.getStatus() == Status.DECLINED) {
            e.getPlayer().disconnect(Component.text("You have to accept the resource pack to play!"));
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
