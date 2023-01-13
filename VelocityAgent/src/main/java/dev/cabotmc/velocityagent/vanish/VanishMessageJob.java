package dev.cabotmc.velocityagent.vanish;

import java.util.ArrayList;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;
import com.velocitypowered.proxy.Velocity;

import dev.cabotmc.vanish.VanishManager;
import dev.cabotmc.velocityagent.VelocityAgent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class VanishMessageJob {
    static Component[] frames = new Component[45];
    static int frame = 0;

    public static void generateThings() {
        var mm = MiniMessage.miniMessage();
        var template = "<gradient:#00F260:#0575E6:%s>You are currently vanished</gradient>";
        for (int i = 44; i >= 0; i--) {
            var amt = (((i * 8) / 180d) - 1.0d) * -1d;
            frames[i] = mm.deserialize(template.replace("%s", Double.toString(amt)));
        }
    }
    public static void runJob() {
        VanishManager.getVanishedPlayers()
                .stream()
                .map(VelocityAgent.getProxy()::getPlayer)
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .forEach(p -> {
                    p.sendActionBar(frames[frame]);
                });
        frame++;
        if (frame >= frames.length) {
            frame = 0;
        }
    }

    static int lerp(int a, int b, double amt) {
        return (int) Math.round(a * amt + b * (1D - amt));
    }
}
