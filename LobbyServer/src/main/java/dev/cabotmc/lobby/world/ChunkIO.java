package dev.cabotmc.lobby.world;

import java.nio.ByteBuffer;


import dev.cabotmc.lobby.Main;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.Block.Getter.Condition;

public class ChunkIO {
    public static byte[] serializeChunk(Chunk c) {
        ByteBuffer b = ByteBuffer.allocate(((c.getMaxSection() - c.getMinSection()) * 16 * 16 * 16 * 2) + 8);
        
        b.putInt(c.getMinSection());
        b.putInt(c.getMaxSection());

        for (int y = c.getMinSection() * 16; y < c.getMaxSection() * 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    b.putShort(c.getBlock(x, y, z, Condition.TYPE).stateId());
                }
            }
        }
        return b.array();
    }
    public static void loadBlocksToChunk(Chunk c, byte[] data) {
        ByteBuffer b = ByteBuffer.wrap(data);

        int minSection = b.getInt();
        int maxSection = b.getInt();

        for (int y = minSection * 16; y < maxSection * 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    c.setBlock(x, y, z, Block.fromStateId(b.getShort()));
                    c.setBiome(x, y, z, Main.CHRISTMAS_BIOME);
                }
            }
        }
    }
}
