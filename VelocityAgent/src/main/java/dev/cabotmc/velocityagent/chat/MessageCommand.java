package dev.cabotmc.velocityagent.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.velocityagent.VelocityAgent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MessageCommand {
    public static BrigadierCommand create() {
        var x = LiteralArgumentBuilder.<CommandSource>literal("msg")
                .then(
                        RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    VelocityAgent.getProxy().getAllPlayers()
                                            .stream()
                                            .map(Player::getUsername)
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(
                                        RequiredArgumentBuilder
                                                .<CommandSource, String>argument("message",
                                                        StringArgumentType.greedyString())
                                                .executes(MessageCommand::messagePlayer)
                                                .build())

                                .build()

                );
        return new BrigadierCommand(x);
    }

    public static int messagePlayer(CommandContext<CommandSource> ctx) {
        var target = ctx.getArgument("name", String.class);
        if (!VelocityAgent.getProxy().getPlayer(target).isPresent()) {
            return -1;
        }
        var msg = ctx.getArgument("message", String.class);
        var targetPlayer = VelocityAgent.getProxy().getPlayer(target).get();
        var sender = (Player) ctx.getSource();
        var isBlocked = BlockCommand.isBlocked(sender.getUniqueId(), targetPlayer.getUniqueId());
        var messages = ChatFormatter.formatDM(msg, sender, targetPlayer, isBlocked);
        sender.sendMessage(messages[0]);
        targetPlayer.sendMessage(messages[1]);
        return 1;
    }
}
