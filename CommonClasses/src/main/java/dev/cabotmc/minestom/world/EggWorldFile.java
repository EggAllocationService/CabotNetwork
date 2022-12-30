package dev.cabotmc.minestom.world;

import com.google.gson.Gson;
import dev.cabotmc.minestom.world.WorldProperties;

import javax.print.attribute.standard.Compression;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EggWorldFile {
    public WorldProperties properties;
    public HashMap<String, byte[]> compressedChunks;
    public void saveToFile(File f) throws Exception {
        Files.delete(f.toPath());
        var out = new ZipOutputStream(new FileOutputStream(f));
        out.setLevel(0);
        out.putNextEntry(new ZipEntry("world.json"));

        var g = new Gson();
        var s = g.toJson(properties);
        out.write(s.getBytes());
        out.closeEntry();
        for (var chunkKey : compressedChunks.keySet()) {
            out.putNextEntry(new ZipEntry(chunkKey + ".dat"));
            out.write(compressedChunks.get(chunkKey));
            out.closeEntry();
        }
        out.flush();
        out.close();
    }
    public static EggWorldFile read(File f) throws Exception {
        if (!f.exists()) return null;
        var in = new ZipInputStream(new FileInputStream(f));
        var w = new EggWorldFile();
        w.compressedChunks = new HashMap<>();
        while (true) {
            var entry = in.getNextEntry();
            if (entry == null) break;
            if (entry.getName().endsWith("json")) {
                var s = new String(in.readAllBytes());
                var g = new Gson();
                w.properties = g.fromJson(s, WorldProperties.class);
            } else {
                // chunk file
                var name = entry.getName().replace(".dat", "");
                var b = in.readAllBytes();
                w.compressedChunks.put(name, b);
            }
        }
        in.close();
        return w;
    }
    public static EggWorldFile create(File f) throws Exception {
        var out = new ZipOutputStream(new FileOutputStream(f));
        out.setLevel(0);
        out.putNextEntry(new ZipEntry("world.json"));
        var w = new EggWorldFile();
        w.properties = new WorldProperties();
        w.compressedChunks = new HashMap<>();
        var g = new Gson();
        var s = g.toJson(w.properties);
        out.write(s.getBytes());
        out.closeEntry();
        out.flush();
        out.close();
        return w;
    }
}
