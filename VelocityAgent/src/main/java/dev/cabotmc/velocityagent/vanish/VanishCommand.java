package dev.cabotmc.velocityagent.vanish;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.vanish.VanishManager;
import net.kyori.adventure.text.Component;

public class VanishCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        var p = (Player) invocation.source();
        var state = VanishManager.getRecord(p.getUniqueId());
        VanishModifier.setVanished(state, !state.vanished);
        p.sendMessage(Component.text(state.vanished ? "You are now vanished" : "You are no longer vanished"));
        
    }
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("vanish.vanish");
    }
}
