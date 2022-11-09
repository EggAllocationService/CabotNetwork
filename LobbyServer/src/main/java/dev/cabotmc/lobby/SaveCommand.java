package dev.cabotmc.lobby;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.command.builder.Command;

public class SaveCommand extends Command {

    public SaveCommand() {
        super("save");
        setDefaultExecutor((sender, context) -> {
            var inst = sender.asPlayer().getInstance();
            inst.saveChunksToStorage();
        
        });
    }
    
}
