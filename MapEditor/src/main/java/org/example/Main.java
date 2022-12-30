package org.example;

import dev.cabotmc.minestom.world.WorldProperties;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

public class Main {
    public static void main(String[] args) {
        var server = MinecraftServer.init();
        System.out.println("No docker detected, enabling online mode");
        MojangAuth.init();
        var fullbright = DimensionType.builder(NamespaceID.from("minestom:full_bright"))
                .ambientLight(2.0f)
                .fixedTime(1000L)
                .build();
        InstanceTracker.init(MinecraftServer.getInstanceManager());
        MinecraftServer.getDimensionTypeManager().addDimension(fullbright);
        MinecraftServer.getCommandManager().register(new SaveCommand());
        MinecraftServer.getCommandManager().register(new UpCommand());
        MinecraftServer.getCommandManager().register(new EditCommand());
        var lobby = InstanceTracker.create("lobby", fullbright);

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, e -> {
            e.setSpawningInstance(lobby);
            var info = new WorldProperties();
            e.getPlayer().setRespawnPoint(new Pos(info.spawnX, info.spawnY, info.spawnZ));
            e.getPlayer().setAllowFlying(true);
            e.getPlayer().setGameMode(GameMode.CREATIVE);
        });
        var port = args.length > 0 ? Integer.parseInt(args[0]) : 25563;
        server.start("0.0.0.0", port);
    }
}