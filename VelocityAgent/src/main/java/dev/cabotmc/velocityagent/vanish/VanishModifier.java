package dev.cabotmc.velocityagent.vanish;

import dev.cabotmc.mgmt.protocol.CrossServerMessage;
import dev.cabotmc.vanish.VanishRecord;
import dev.cabotmc.velocityagent.VelocityAgent;

public class VanishModifier {
    public static void setVanished(VanishRecord r, boolean vanished) {
        r.setVanished(vanished);
        var msg = new CrossServerMessage();
        msg.data = "vanish_update";
        msg.targets = new String[]{"*"};
        msg.from = "velocity";
        VelocityAgent.kryoClient.sendTCP(msg);
    }
}
