package dev.cabotmc.velocityagent.queue;

import java.util.Collection;
import java.util.HashMap;

import com.velocitypowered.api.proxy.Player;

public class QueueManager {
    static HashMap<String, Queue> gamemodes = new HashMap<>();
    static {
        addGamemode(new SoloHCGamemode());
        addGamemode(new EbeMcGamemode());
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
    public static Queue getQueue(Player p) {
        for (var q : gamemodes.values()) {
            if (q.isInQueue(p)) return q;
        }
        return null;
    }
    public static Queue getGameMode(String name) {
        return gamemodes.get(name);
    }
    public static Collection<Queue> getGameModes() {
        return gamemodes.values();
    }
}
