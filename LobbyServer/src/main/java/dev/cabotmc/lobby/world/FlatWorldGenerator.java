package dev.cabotmc.lobby.world;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;

public class FlatWorldGenerator implements Generator {

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        unit.modifier().fillHeight(0, 64, Block.STONE);
        
    }
    
}
