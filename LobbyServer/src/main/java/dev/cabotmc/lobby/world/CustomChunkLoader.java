package dev.cabotmc.lobby.world;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;

public class CustomChunkLoader implements IChunkLoader {

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        var str = getChunkKey(chunkX, chunkZ);
        if (!(new File(str + ".dat")).exists()) {
            return CompletableFuture.completedFuture(null);
        }
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of(str + ".dat"));
        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
        var c = ((InstanceContainer) instance).getChunkSupplier().createChunk(instance, chunkX, chunkZ);
        ChunkIO.loadBlocksToChunk(c, bytes);
        return CompletableFuture.completedFuture(c);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        var bytes = ChunkIO.serializeChunk(chunk);
        try {
            if (new File(getChunkKey(chunk)).exists()) {
                Files.write(Path.of(getChunkKey(chunk) + ".dat"), bytes, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.write(Path.of(getChunkKey(chunk) + ".dat"), bytes, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }
    @Override
    public boolean supportsParallelLoading() {
        return true;
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
