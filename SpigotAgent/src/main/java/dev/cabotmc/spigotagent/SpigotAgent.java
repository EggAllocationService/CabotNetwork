package dev.cabotmc.spigotagent;

import com.esotericsoftware.kryonet.Client;
import dev.cabotmc.mgmt.ProtocolHelper;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;

public final class SpigotAgent extends JavaPlugin {
    public static Client kryoClient;

    @Override
    public void onEnable() {
        // Plugin startup logic
        kryoClient = new Client();
        ProtocolHelper.registerClasses(kryoClient.getKryo());
        kryoClient.start();
        try {
            kryoClient.connect(5000, "172.17.0.1", 3269);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var instanceName = System.getenv("CABOT_NAME");
        var connectPort = Bukkit.getPort();
        var msg = new ServerStatusChangeMessage();
        msg.connectAddress = null;
        msg.serverName = instanceName;
        msg.online = true;
        msg.connectPort = connectPort;
        SpigotAgent.kryoClient.sendTCP(msg);
    }

    @Override
    public void onDisable() {
        var instanceName = System.getenv("CABOT_NAME");
        var connectPort = Bukkit.getPort();
        var msg = new ServerStatusChangeMessage();
        msg.connectAddress = null;
        msg.serverName = instanceName;
        msg.online = false;
        msg.connectPort = connectPort;
        kryoClient.sendTCP(msg);
        kryoClient.close();
    }
}
