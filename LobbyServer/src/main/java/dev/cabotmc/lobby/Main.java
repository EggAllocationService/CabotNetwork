package dev.cabotmc.lobby;

import java.io.IOException;
import java.util.ArrayList;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.lobby.db.Database;
import dev.cabotmc.lobby.world.FlatWorldGenerator;
import dev.cabotmc.lobby.world.InstanceTracker;
import dev.cabotmc.lobby.world.ZipFileChunkLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.PlacementRules;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.Biome.Category;
import net.minestom.server.world.biomes.Biome.Precipitation;


public class Main {
    public static MinecraftServer server;
    public static Biome CHRISTMAS_BIOME;
    public static Team WINNER_TEAM;
    public static int tickNo = 0;
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
            event.getPlayer().setRespawnPoint(new Pos(0, System.getenv().containsKey("IS_LIMBO") ? 18 : 111, 0));
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
            event.getPlayer().setAllowFlying(true);
        });
        if (System.getenv().containsKey("IS_LIMBO")) {
            MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e -> {
                e.getPlayer().showTitle(
                    Title.title(
                        Component.text(""),
                        Component.text("Processing your skin, please wait", TextColor.color(0xca4040))
                            .decorate(TextDecoration.BOLD)
                        )
                );
             });
        } else {
            MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e -> {
                LeaderbordGenerator.showLeaderboard(e.getPlayer()); 
             });
             
             MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e -> {
                WINNER_TEAM.removeMember(e.getPlayer().getUsername());
             });
             lobby.getWorldBorder().setDiameter(100);
             lobby.getWorldBorder().setCenter(0, 0);
             
             Database.init();
             WINNER_TEAM = MinecraftServer.getTeamManager().createTeam("winners");
             WINNER_TEAM.setTeamColor(NamedTextColor.GOLD);
             MinecraftServer.getGlobalEventHandler().addListener(InstanceTickEvent.class, e -> {
                var ppl = WINNER_TEAM.getMembers()
                    .stream()
                    .map(c -> MinecraftServer.getConnectionManager().getPlayer(c))
                    .filter(c -> c != null)
                    .toList();
                for (var p : ppl) {
                    tickNo ++;
                    if (tickNo > 10) {
                        var packet = ParticleCreator.createParticlePacket(
                            Particle.WAX_ON,
                            true,
                            p.getPosition().x(),
                            p.getPosition().y() + 1,
                            p.getPosition().z(),
                            0.3f, 
                            0.3f, 
                            0.3f,
                            1f,
                            5,
                            null);
                        p.sendPacketToViewersAndSelf(packet);
                        tickNo = 0;
                    }
                }    
            
             });
            
        }
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
