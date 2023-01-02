package dev.cabotmc.velocityagent.queue;

import java.util.Collection;
import java.util.HashMap;

import com.velocitypowered.api.proxy.Player;

public class QueueManager {
    static HashMap<String, Queue> gamemodes = new HashMap<>();
    static {
        addGamemode(new SoloHCGamemode());
        addGamemode(new EbeMcGamemode());
        addGamemode(new UhcQueue());
        addGamemode(new DepartmasGamemode());
        addGamemode(new SkyItemGamemode());
    }
    public static void addGamemode(Queue q) {
        gamemodes.put(q.getName(), q);
    }
    public static void serverReady(String server, String token) {
        for (var q : gamemodes.values()) {
            if (q.waitingForToken(token)) {
                q.onServerCreate(server, token);
                break;
            }
        }
    }
    public static void serverMessage(String msg, String from) {
        var things = msg.split(":");
        var q = gamemodes.get(things[1]);
        if (q == null) return;
        q.onServerMessage(from, msg);
        
    }
    public static Queue getQueue(Player p) {
        for (var q : gamemodes.values()) {
            if (q.isInQueue(p)) return q;
        }
        return null;
    }
    public static void removeQueue(Player p) {
        for (var q : gamemodes.values()) {
            q.removePlayer(p);
        }
    }
    public static Queue getGameMode(String name) {
        return gamemodes.get(name);
    }
    public static Collection<Queue> getGameModes() {
        return gamemodes.values();
    }
}
