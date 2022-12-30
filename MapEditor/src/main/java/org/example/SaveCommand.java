package org.example;

import dev.cabotmc.minestom.Biomes;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.extras.PlacementRules;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;

public class SaveCommand extends Command {

    public SaveCommand() {
        super("save");
        setDefaultExecutor((sender, context) -> {
            var inst = sender.asPlayer().getInstance();
            inst.saveChunksToStorage().thenRun(() -> {
                sender.sendMessage(Component.text("Saved chunks to world file"));
            });

        });
    }

}

