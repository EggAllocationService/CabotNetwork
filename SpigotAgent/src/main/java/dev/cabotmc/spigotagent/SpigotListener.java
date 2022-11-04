package dev.cabotmc.spigotagent;

import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class SpigotListener implements Listener {
    @EventHandler
    public void ready(WorldLoadEvent e) {
        var instanceName = System.getenv("CABOT_NAME");
        var connectPort = Bukkit.getPort();
        var msg = new ServerStatusChangeMessage();
        msg.connectAddress = null;
        msg.serverName = instanceName;
        msg.online = true;
        msg.connectPort = connectPort;
        SpigotAgent.kryoClient.sendTCP(msg);
    }
}
