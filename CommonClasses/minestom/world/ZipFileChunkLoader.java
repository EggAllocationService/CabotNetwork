package dev.cabotmc.minestom.world;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;

public class ZipFileChunkLoader implements IChunkLoader {
    public WorldProperties getProperties() {
        return world.properties;
    }
    EggWorldFile world;
    String fileName;
    public ZipFileChunkLoader(String fileName) {
        try {
            if (!Files.exists(Path.of(fileName))) {
                world = EggWorldFile.create(new File(fileName));
            } else {
                world = EggWorldFile.read(new File(fileName));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.fileName = fileName;

    }
    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        if (!world.compressedChunks.containsKey(getChunkKey(chunkX, chunkZ))) {
            return CompletableFuture.completedFuture(null);
        }
        var uncompressed = decompress(world.compressedChunks.get(getChunkKey(chunkX, chunkZ)));
        var c = ((InstanceContainer) instance).getChunkSupplier().createChunk(instance, chunkX, chunkZ);
        ChunkIO.loadBlocksToChunk(c, uncompressed);
        return CompletableFuture.completedFuture(c);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        var out = new ByteArrayOutputStream();
        var deflOut = new DeflaterOutputStream(out);
        try {
            ChunkIO.serializeChunk(chunk, deflOut);
            world.compressedChunks.put(getChunkKey(chunk), compress(out.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunks(@NotNull Collection<Chunk> chunks) {
        for (var c : chunks) {
            saveChunk(c);
        }
        try {
            world.saveToFile(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public static String getChunkKey(int x, int z) {
        return x + "_" + z;
    }
    public static String getChunkKey(Chunk c) {
        return getChunkKey(c.getChunkX(), c.getChunkZ());
    }
    public static byte[] compress(byte[] in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            defl.write(in);
            defl.flush();
            defl.close();

            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(150);
            return null;
        }
    }

    public static byte[] decompress(byte[] in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InflaterOutputStream infl = new InflaterOutputStream(out);
            infl.write(in);
            infl.flush();
            infl.close();

            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(150);
            return null;
        }
    }

}
