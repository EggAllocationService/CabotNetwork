package dev.cabotmc.commonnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import dev.cabotmc.mgmt.ProtocolHelper;
import dev.cabotmc.mgmt.protocol.ClientIdentifyMessage;
import dev.cabotmc.mgmt.protocol.CrossServerMessage;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;

public class CommonClient {
    static Client kryoClient;
    static Thread hook = new ShutdownThread();
    static ArrayList<Consumer<CrossServerMessage>> listeners = new ArrayList<>();

    public static void init() throws IOException {
        kryoClient = new Client();
        ProtocolHelper.registerClasses(kryoClient.getKryo());
        kryoClient.start();
        kryoClient.connect(5000, "172.17.0.1", 3269);
        kryoClient.addListener(new Listener() {
            @Override
            public void received(Connection c, Object o) {
                if (o instanceof CrossServerMessage) {
                    for (var l : listeners) {
                        l.accept((CrossServerMessage) o);
                    }
                }
            }
        });
        var identifyMsg = new ClientIdentifyMessage();
        identifyMsg.instanceName = System.getenv("CABOT_NAME");
        kryoClient.sendTCP(identifyMsg);
    }

    public static void sayHello(int connectPort) {
        var instanceName = System.getenv("CABOT_NAME");
        var msg = new ServerStatusChangeMessage();
        msg.connectAddress = null;
        msg.serverName = instanceName;
        msg.online = true;
        msg.connectPort = connectPort;
        kryoClient.sendTCP(msg);
    }

    public static Thread getShutdownHook() {
        return hook;
    }

    public static void addMessageHandler(Consumer<CrossServerMessage> e) {
        listeners.add(e);
    }

    public static void sendMessageToServer(String serverName, String message) {
        sendMessageToServers(new String[]{serverName}, message);
    }

    public static void sendMessageToServers(String[] servers, String message) {
        var msg = new CrossServerMessage();
        msg.targets = servers;
        msg.data = message;
        msg.from = System.getenv("CABOT_NAME");
        kryoClient.sendTCP(msg);
    }

    static class ShutdownThread extends Thread {
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
