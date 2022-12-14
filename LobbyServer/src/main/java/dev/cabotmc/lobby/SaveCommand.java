package dev.cabotmc.lobby;

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
