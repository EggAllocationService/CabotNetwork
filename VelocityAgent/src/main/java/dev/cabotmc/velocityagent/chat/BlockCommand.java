package dev.cabotmc.velocityagent.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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

public class BlockCommand {
    static HashMap<UUID, ArrayList<UUID>> blockedPlayers = new HashMap<>();
    public static BrigadierCommand create() {
        var x = LiteralArgumentBuilder.<CommandSource>literal("block")
            .requires(source -> source.hasPermission("block.use"))
            .then(
                RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.string())
                .suggests((context, builder) -> {
                    VelocityAgent.getProxy().getAllPlayers()
                        .stream()
                        .map(Player::getUsername)
                        .forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .requires(source -> source.hasPermission("block.use"))
                .executes(BlockCommand::togglePlayerBlock)
                .build()
            );

        return new BrigadierCommand(x);
    }   
    public static int togglePlayerBlock(final CommandContext<CommandSource> context) {
        var argument = context.getArgument("name", String.class);
        var p = VelocityAgent.getProxy().getPlayer(argument);
        if (p.isEmpty()) return -1;
        var toBlock = p.get().getUniqueId();
        var source = (Player) context.getSource();
        Component toSend;
        if (getBlockList(source.getUniqueId()).contains(toBlock)) {
            getBlockList(source.getUniqueId()).remove(toBlock);
            toSend = Component.text("Successfully unblocked " + p.get().getUsername(), TextColor.color(0xf54242));
        } else {
            getBlockList(source.getUniqueId()).add(toBlock);
            toSend = Component.text("Successfully blocked " + p.get().getUsername(), TextColor.color(0xf54242));
        }
        source.sendMessage(toSend);
        return 1;
    }   
    static ArrayList<UUID> getBlockList(UUID player) {
        if (!blockedPlayers.containsKey(player)) {
            blockedPlayers.put(player, new ArrayList<>());
        }
        return blockedPlayers.get(player);
    }
    public static boolean isBlocked(UUID sender, UUID reciever) {
        return getBlockList(reciever).contains(sender);
    }
}
