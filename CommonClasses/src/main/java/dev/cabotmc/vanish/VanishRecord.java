package dev.cabotmc.vanish;

import org.bson.codecs.pojo.annotations.BsonId;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class VanishRecord {
     @BsonId
     public String UUID;
     public boolean vanished = false;
     public boolean pickupItems = false;
     public boolean chatEnabled = true;
     public void setVanished(boolean v) {
          VanishManager.getCollection().updateOne(Filters.eq("_id", UUID), Updates.set("vanished", v));
          vanished = v;
     }
     
}
