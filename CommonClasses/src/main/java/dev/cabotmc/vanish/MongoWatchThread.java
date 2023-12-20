package dev.cabotmc.vanish;

import java.util.UUID;

import com.mongodb.client.model.changestream.FullDocument;

public class MongoWatchThread extends Thread {
    public MongoWatchThread() {
        super("MongoDB Watcher");
    }
    @Override
    public void run() {
        var stream = VanishManager.getCollection().watch().fullDocument(FullDocument.WHEN_AVAILABLE);
        var itr = stream.iterator();
        while(true) {
            var changed = itr.tryNext();
            if (changed != null) {
                var c = changed.getFullDocument();
                if (c == null) continue;

            }


            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void handleUpdate(VanishRecord c) {
        var u = UUID.fromString(c.UUID);
        if (!VanishManager.players.containsKey(u)) {
            // add to collection
            VanishManager.players.put(u, c);
            if (c.vanished && VanishManager.getPlatformProvider().isOnline(u)) {
                VanishManager.getPlatformProvider().vanishPlayer(u);
            }
        } else {
            var old = VanishManager.players.get(u);
            old.chatEnabled = c.chatEnabled;
            old.pickupItems = c.pickupItems;
            if (!old.vanished && c.vanished ) {
                // vanish the player
                old.vanished = c.vanished;
                if (VanishManager.getPlatformProvider().isOnline(u)) {
                    VanishManager.getPlatformProvider().vanishPlayer(u);
                }
            } else if (old.vanished && !c.vanished) {
                // unvanish player
                old.vanished = c.vanished;
                if (VanishManager.getPlatformProvider().isOnline(u)) {
                    VanishManager.getPlatformProvider().unvanishPlayer(u);
                }
            }
        }
    }
}
