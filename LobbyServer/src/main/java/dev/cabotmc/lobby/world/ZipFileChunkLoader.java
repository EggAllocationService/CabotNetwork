package dev.cabotmc.lobby.world;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;

public class ZipFileChunkLoader implements IChunkLoader {
    public ZipFile handle;
    public ZipFileChunkLoader(String fileName) {
        try {
            handle = new ZipFile(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public static String getChunkKey(int x, int z) {
        return x + "_" + z;
    }
    public static String getChunkKey(Chunk c) {
        return getChunkKey(c.getChunkX(), c.getChunkZ());
    }


    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        var str = getChunkKey(chunkX, chunkZ);
        if (handle.getEntry(str + ".dat") == null) {
            return CompletableFuture.completedFuture(null);
        }
        InputStream bytes;
        try {
            bytes = handle.getInputStream(handle.getEntry(str + ".dat"));
        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
        var c = ((InstanceContainer) instance).getChunkSupplier().createChunk(instance, chunkX, chunkZ);
        try {
            ChunkIO.loadBlocksToChunk(c, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bytes.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(c);
    }
    @Override
    public CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        
        return CompletableFuture.completedFuture(null);
    }
}
