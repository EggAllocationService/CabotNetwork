package dev.cabotmc.spigotagent.elo;

import java.util.HashMap;
import java.util.UUID;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bukkit.entity.Player;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import forwardloop.glicko2s.EloResult;
import forwardloop.glicko2s.Glicko2;
import forwardloop.glicko2s.Glicko2J;

public class EloService {
    static CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    static CodecRegistry pojoCodecRegistry;
    public static MongoClient client;
    static MongoDatabase mc;
    static MongoCollection<StoredRating> ratings = mc.getCollection("uhcratings", StoredRating.class);
    static HashMap<UUID, StoredRating> cache = new HashMap<>();
    static HashMap<UUID, HashMap<UUID, EloResult>> results = new HashMap<>();
    public static void init() {
        pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        String uri = "mongodb://minecraft:crafting@172.17.0.1:27017/?authSource=mc";
        client = MongoClients.create(uri);
        mc = client.getDatabase("mc").withCodecRegistry(pojoCodecRegistry);
        
    }
    public static HashMap<UUID, EloResult> getResults(UUID target) {
        if (!results.containsKey(target)) {
            results.put(target, new HashMap<>());
        } 
        return results.get(target);
    }
    public static void recordResult(UUID winner, UUID loser) {
        getResults(winner).putIfAbsent(loser, Glicko2J.Win);
        getResults(loser).putIfAbsent(winner, Glicko2J.Loss);
    }
    public static void updateRating(String uuid) {
        ratings.replaceOne(Filters.eq("_id", uuid), cache.get(UUID.fromString(uuid)));
    }

    public static StoredRating getRating(UUID user) {
        if (cache.containsKey(user)) {
            return cache.get(user);
        }
        var found = ratings.find(Filters.eq("_id", user.toString())).first();
        if (found == null) {
            var created = StoredRating.newRating(user);
            ratings.insertOne(created);
            cache.put(user, created);
            return created;
        } else {
            cache.put(user, found);
            return found;
        }
    }
    public static class StoredRating {
        @BsonId
        public String id;
        public double rating;
        public double ratingDeviation;
        public double volatility;
        public StoredRating(UUID user, Glicko2 in) {
            rating = in.rating();
            ratingDeviation = in.ratingDeviation();
            volatility = in.ratingVolatility();
            id = user.toString();
        }
        public StoredRating() {}
        public Player getPlayer() {
            return Bukkit.getPlayer(UUID.fromString(id));

        }

        public Glicko2 toGlicko() {
            
            return new Glicko2(rating, ratingDeviation, volatility);
        }
        public void copyRatingsFromGlicko(Glicko2 input) {
            rating = input.rating();
            ratingDeviation = input.ratingDeviation();
            volatility = input.ratingVolatility();
        }
        public static StoredRating newRating(UUID u) {
            return new StoredRating(u, Glicko2J.newPlayerRating());

        }
    }
    
}
