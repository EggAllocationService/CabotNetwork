package dev.cabotmc.lobby;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;

import dev.cabotmc.lobby.world.FlatWorldGenerator;
import dev.cabotmc.lobby.world.InstanceTracker;
import dev.cabotmc.mgmt.ProtocolHelper;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

public class Main {
    public static MinecraftServer server;
    public static Client kryoClient;
    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");
        server = MinecraftServer.init();
        InstanceTracker.init(MinecraftServer.getInstanceManager());
        var fullbright = DimensionType.builder(NamespaceID.from("minestom:full_bright"))
        .ambientLight(2.0f)
        .fixedTime(1000L)
        .build();
        MinecraftServer.getDimensionTypeManager().addDimension(fullbright);
        var lobby = InstanceTracker.create("lobby", fullbright);
        lobby.setGenerator(new FlatWorldGenerator());
        lobby.getWorldBorder().setDiameter(100);
        lobby.getWorldBorder().setCenter(0, 0);
        if (System.getenv().containsKey("CABOT_NAME")) {
            System.out.println("Enabling managment connection");
            // running in docker
            VelocityProxy.enable("RJtJ5WqA9As8");

            kryoClient = new Client();
            ProtocolHelper.registerClasses(kryoClient.getKryo());
            kryoClient.start();
            kryoClient.connect(5000, "172.17.0.1", 3269);
            var instanceName = System.getenv("CABOT_NAME");
            var msg = new ServerStatusChangeMessage();
            msg.connectAddress = null;
            msg.serverName = instanceName;
            msg.online = true;
            msg.connectPort = 25566;
            kryoClient.sendTCP(msg);
            Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        } else {
            System.out.println("No docker detected, enabling online mode");
            MojangAuth.init();
        }
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(InstanceTracker.get("lobby"));
            event.getPlayer().setRespawnPoint(new Pos(0, 65, 0));
            event.getPlayer().setGameMode(GameMode.CREATIVE);
        });
        server.start("0.0.0.0", 25566);
    }
    public static class ShutdownThread extends Thread {
        @Override
        public void run() {
        var instanceName = System.getenv("CABOT_NAME");
        var msg = new ServerStatusChangeMessage();
        msg.connectAddress = null;
        msg.serverName = instanceName;
        msg.online = false;
        msg.connectPort = 25566;
        kryoClient.sendTCP(msg);
        }
    }
}
