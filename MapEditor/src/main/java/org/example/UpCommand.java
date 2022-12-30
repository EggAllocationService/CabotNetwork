package org.example;

import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class UpCommand extends Command  {
    public UpCommand() {
        super("up");
        setDefaultExecutor((sender, context) -> {
            var p = sender.asPlayer();
            p.getInstance().setBlock(p.getPosition().blockX(),p.getPosition().blockY(),p.getPosition().blockZ(), Block.BEDROCK);
        });
    }
}
