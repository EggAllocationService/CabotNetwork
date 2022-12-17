package dev.cabotmc.velocityagent.santahat;

import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.util.GameProfile;

import dev.cabotmc.skins.SkinReplacementRecord;
import dev.cabotmc.velocityagent.db.Database;

public class SanataManager {
    public static ArrayList<UUID> sendToLimbo = new ArrayList<>();
    public static void findReplacement(GameProfileRequestEvent e) {
        var orig = e.getOriginalProfile().getProperties().get(0);
        var url = extractSkinUrl(orig.getValue());
        System.out.println(url);
        SkinReplacementRecord first = Database.skins.find(Filters.eq("_id", url)).first();
        if (first != null) {
            System.out.println("Skin already present, replacing normally");
            var replacement = new ArrayList<GameProfile.Property>();
            replacement.add(new GameProfile.Property("textures", first.replacementValue, first.replacementSignature));
            var r = e.getOriginalProfile().withProperties(replacement);
            e.setGameProfile(r);
        } else {
            // no replacement found
            System.out.println("Generating santa skin");
            sendToLimbo.add(e.getOriginalProfile().getId());
            SantaThread.addJob(new SantaJob(e.getGameProfile().getId().toString(), url));
        }
    }
    public static String extractSkinUrl(String value) {
        var json = new String(Base64.getDecoder().decode(value));
        var g = new Gson();
        var j = (JsonObject) g.fromJson(json, JsonObject.class);
        var textures = j.getAsJsonObject("textures");
        var skin = textures.getAsJsonObject("SKIN");
        return skin.get("url").getAsString();
    }
}
