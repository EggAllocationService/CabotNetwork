package dev.cabotmc.vanish;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.mongodb.client.MongoCollection;

import dev.cabotmc.commonnet.CommonClient;

public class VanishManager {
    static MongoCollection<VanishRecord> vanish;
    static HashMap<UUID, VanishRecord> players = new HashMap<>();
    static VanishPlatformProvider p;
    static MongoWatchThread watcher;
    public static void init(MongoCollection<VanishRecord> e) {
        vanish = e;
    
        for (VanishRecord r : e.find()) {
            var u = UUID.fromString(r.UUID);
            players.put(u, r);
        }
    }
    public static void startWatcher(VanishPlatformProvider provider) {
        if (watcher != null) return;
        p = provider;
        CommonClient.addMessageHandler(msg -> {
            if (msg.data.equals("vanish_update")) {
                System.out.println("updaing vanish state");
                getPlatformProvider().scheduleTaskSync(() -> {
                    for (var x : getCollection().find()) {
                        MongoWatchThread.handleUpdate(x);
                    }
                });
            }
        });
        /*watcher = new MongoWatchThread();
        watcher.setDaemon(true);
        watcher.start();*/
        
    }
    public static MongoCollection<VanishRecord> getCollection() {
        return vanish;
    }
    public static VanishPlatformProvider getPlatformProvider() {
        return p;
    }
    public static boolean isVanished(UUID u) {
        return players.containsKey(u) && players.get(u).vanished;
    }
    public static VanishRecord getRecord(UUID u) {
        if (players.containsKey(u)) return players.get(u);
        var r = new VanishRecord();
        r.UUID = u.toString();
        vanish.insertOne(r);
        players.put(u, r);
        return r;
    }
    public static List<UUID> getVanishedPlayers() {
        return players.keySet().stream()
            .filter(c -> players.get(c).vanished)
            .toList();
    }

}
