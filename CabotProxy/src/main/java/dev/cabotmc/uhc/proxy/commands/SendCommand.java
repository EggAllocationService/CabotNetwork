package dev.cabotmc.uhc.proxy.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.cabotmc.uhc.proxy.TransitionEffects;

public class SendCommand {
    public static BrigadierCommand create(final ProxyServer proxy, final TransitionEffects fx) {

        var node = BrigadierCommand.literalArgumentBuilder("send")
                .requires(x -> x.hasPermission("cabot.command.send"))
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            proxy.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUsername()
                            ));

                            builder.suggest("all");
                            return builder.buildFuture();
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("server", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    proxy.getAllServers().forEach(s -> builder.suggest(s.getServerInfo().getName()));
                                    return builder.buildFuture();
                                })
                                .executes(cmd -> {
                                    var player = cmd.getArgument("player", String.class);
                                    var target = cmd.getArgument("server", String.class);

                                    var targetServer = proxy.getServer(target).orElse(null);
                                    if (targetServer == null) return 0;
                                    if (player.equals("all")) {
                                        proxy.getAllPlayers().forEach(p -> fx.transferPlayer(p, targetServer));
                                    } else {
                                        // try to get player
                                        var p = proxy.getPlayer(player).orElse(null);
                                        if (p == null) return 0;
                                        fx.transferPlayer(p, targetServer);
                                    }

                                    return 1;
                                })
                                .build())
                        .build());

        return new BrigadierCommand(node);
    }
}
