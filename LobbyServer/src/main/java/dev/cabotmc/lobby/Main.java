package dev.cabotmc.lobby;

import java.io.IOException;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.lobby.world.FlatWorldGenerator;
import dev.cabotmc.lobby.world.InstanceTracker;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.PlacementRules;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket.Reason;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.Biome.Category;
import net.minestom.server.world.biomes.Biome.Precipitation;


public class Main {
    public static MinecraftServer server;
    public static Biome CHRISTMAS_BIOME;
    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");
        server = MinecraftServer.init();
        InstanceTracker.init(MinecraftServer.getInstanceManager());
        var fullbright = DimensionType.builder(NamespaceID.from("minestom:full_bright"))
        .ambientLight(2.0f)
        .fixedTime(1000L)
        .build();
        var bfx = BiomeEffects.builder()
            .grassColor(0xFFFFFF)
            .build();
        CHRISTMAS_BIOME = Biome.builder()
                            .precipitation(Precipitation.NONE)
                            .downfall(1f)
                            .temperature(0.15f)
                            .category(Category.ICY)
                            .name(NamespaceID.from("egg", "departmasland"))
                            .build();
        MinecraftServer.getBiomeManager().addBiome(CHRISTMAS_BIOME);
        
        PlacementRules.init();
        MinecraftServer.getDimensionTypeManager().addDimension(fullbright);
        var lobby = InstanceTracker.create("lobby", fullbright);
        lobby.setGenerator(new FlatWorldGenerator());
        lobby.getWorldBorder().setDiameter(100);
        lobby.getWorldBorder().setCenter(0, 0);
        
        
        MinecraftServer.getCommandManager().register(new SaveCommand());
        if (System.getenv().containsKey("CABOT_NAME")) {
            System.out.println("Enabling managment connection");
            // running in docker
            VelocityProxy.enable("RJtJ5WqA9As8");

            CommonClient.init();
            CommonClient.sayHello(25561);
            
            Runtime.getRuntime().addShutdownHook(CommonClient.getShutdownHook());
        } else {
            System.out.println("No docker detected, enabling online mode");
            MojangAuth.init();
        }
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(InstanceTracker.get("lobby"));
            event.getPlayer().setRespawnPoint(new Pos(0, 65, 0));
            event.getPlayer().setGameMode(GameMode.CREATIVE);
        });
        server.start("0.0.0.0", 25561);
    }

    public static void initStairs() {
        for (var b : Block.values()) {
        
            if (b.namespace().asString().endsWith("stairs")) {
                MinecraftServer.getBlockManager().registerBlockPlacementRule(new SexPlacementRule(b));
            }
        }

    }
    
}
