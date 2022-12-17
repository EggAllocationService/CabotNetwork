package dev.cabotmc.velocityagent;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class LobbyCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) return;
        var p = (Player) invocation.source();
        var lobby = VelocityAgent.getProxy().getServer("lobby").get();
        p.createConnectionRequest(lobby).fireAndForget();
    }
}
