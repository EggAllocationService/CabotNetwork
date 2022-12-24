package dev.cabotmc.random;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CachedRelationships {
    public static HashMap<String, ArrayList<String>> things = new HashMap<>();

    public static void storeMatch(String provider, String item) {
        if (!things.containsKey(item)) {
            things.put(item, new ArrayList<>());
        }
        things.get(item).add(provider);
    }
    public static void saveFile() {
        var g = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.writeString(Path.of("computed.json"), g.toJson(things), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
