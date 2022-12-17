package dev.cabotmc.hardcore;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import dev.cabotmc.chc.CompHardcorePlayer;
import dev.cabotmc.hardcore.points.PointsManager;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.Arrays;

import org.bson.codecs.configuration.CodecProvider;


public class Database {
    static CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    static CodecRegistry pojoCodecRegistry;
    public static MongoClient client;
    static MongoDatabase mc;
    public static MongoCollection<CompHardcorePlayer> comphc;
    public static void init() {
        pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        String uri = "mongodb://minecraft:crafting@172.17.0.1:27017/?authSource=mc";
        client = MongoClients.create(uri);
        mc = client.getDatabase("mc").withCodecRegistry(pojoCodecRegistry);
        comphc = mc.getCollection("comphc", CompHardcorePlayer.class);
    }
    public static void updateScore() {
        PointsManager.round();
        var f = comphc.find(eq("_id", HardcorePlugin.ownerUUID));
        if (f.first() == null) {
            var o = new CompHardcorePlayer();
            o.bestScore = PointsManager.points;
            o.displayName = HardcorePlugin.ownerName;
            o.id = HardcorePlugin.ownerUUID;
            comphc.insertOne(o);
        } else {
            comphc.updateOne(eq("_id", HardcorePlugin.ownerUUID), Updates.max("bestScore", PointsManager.points));
            comphc.updateOne(eq("_id", HardcorePlugin.ownerUUID), Updates.set("displayName", HardcorePlugin.ownerName));
        }
    }
    public static void notifyIfBetter() {
        var f = comphc.find(eq("_id", HardcorePlugin.ownerUUID));
        var x = f.first();
        if (x == null || x.bestScore < PointsManager.points) {
            var msg = Component.text("Your new score of " + PointsManager.points + " beat your previous score!", TextColor.color(0x33d486));
            Bukkit.getServer().sendMessage(msg);
        }
    }
    public static void notifyBest() {
        var f = comphc.find(eq("_id", HardcorePlugin.ownerUUID));
        var x = f.first();
        if (x != null) {
            x.bestScore = Math.round(x.bestScore * 100) / 100;
            var msg = Component.text("Your best score is " + x.bestScore + ". Beat it!", TextColor.color(0x33d486));
            Bukkit.getServer().sendMessage(msg);
        }
    }
}