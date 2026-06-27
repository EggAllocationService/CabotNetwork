package dev.cabotmc.uhc.proxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.cabotmc.uhc.proxy.TransitionEffects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerCommand implements SimpleCommand {
    private TransitionEffects fx;
    private ProxyServer server;
    public ServerCommand(ProxyServer server, TransitionEffects fx) {
        this.fx = fx;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length != 1) {
            invocation.source().sendMessage(Component.text("Usage: /server <server>", NamedTextColor.RED));
            return;
        }
        var targetName = invocation.arguments()[0];
        var target = server.getServer(targetName);
        if (target.isEmpty()) {
            invocation.source().sendMessage(Component.text("No server found with name " + targetName, NamedTextColor.RED));
            return;
        }

        if (invocation.source() instanceof Player p) {
            fx.transferPlayer(p, target.get());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 0) {
            return server.getAllServers().stream().map(s -> s.getServerInfo().getName()).toList();
        } else if (invocation.arguments().length == 1){
            var partial = invocation.arguments()[0];
            return server.getAllServers().stream().filter(s -> s.getServerInfo().getName().toLowerCase().startsWith(partial.toLowerCase())).map(s -> s.getServerInfo().getName()).toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("cabot.command.server");
    }
}
