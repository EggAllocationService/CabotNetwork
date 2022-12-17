package dev.cabotmc.velocityagent.db;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import dev.cabotmc.chc.CompHardcorePlayer;
import dev.cabotmc.skins.SkinReplacementRecord;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;


public class Database {
    static CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    static CodecRegistry pojoCodecRegistry;
    public static MongoClient client;
    static MongoDatabase mc;
    public static MongoCollection<SkinReplacementRecord> skins;
    public static void init() {
        pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        String uri = "mongodb://minecraft:crafting@172.17.0.1:27017/?authSource=mc";
        client = MongoClients.create(uri);
        mc = client.getDatabase("mc").withCodecRegistry(pojoCodecRegistry);
        skins= mc.getCollection("skins", SkinReplacementRecord.class);
    }
}