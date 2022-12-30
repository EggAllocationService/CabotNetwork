package org.example;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.NotNull;

public class EditCommand extends Command {
    public EditCommand() {
        super("edit");
        var nameArg = ArgumentType.String("name");
        addSyntax((sender, context) -> {
            InstanceContainer target;
            var name = context.get(nameArg);
            if (InstanceTracker.get(name) == null) {
                target = InstanceTracker.create(name);
            } else {
                target = InstanceTracker.get(name);
            }
            var p = sender.asPlayer();
            var props = InstanceTracker.worldProps.get(name);
            p.setInstance(target, new Pos(props.spawnX, props.spawnY, props.spawnZ));
            p.setFlying(true);
        }, nameArg);
    }
}
