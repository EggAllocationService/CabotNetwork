package dev.cabotmc.hardcore;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.hardcore.difficulty.BaseDifficulty;
import dev.cabotmc.hardcore.difficulty.DifficultyMenu;
import dev.cabotmc.hardcore.points.BasicPointsListener;
import dev.cabotmc.hardcore.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.io.IOException;

public final class HardcorePlugin extends JavaPlugin {
    public static String ownerName;
    public static String ownerUUID;
    public static HardcorePlugin instance;
    public static Team SPECTATOR_TEAM;
    public static boolean world_ready = false;
    public static Team MINIBOSS_TEAM;
    public static BaseDifficulty difficulty;
    public static boolean allowSpectators = true;

    @Override
    public void onEnable() {
        instance = this;
        Database.init();
        Bukkit.getPluginManager().registerEvents(new BasicListener(), this);
        ownerName = (String) System.getenv().getOrDefault("HC_OWNER", "ThatOneGamer999");
        getLogger().info("Set owner to " + ownerName);
        PointsManager.init();
        if (System.getenv().containsKey("CABOT_NAME")) {
            try {
                CommonClient.init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CommonClient.sayHello(Bukkit.getPort());
            var msg = "hcready:" + ownerName + ":" + System.getenv("CABOT_NAME");
            CommonClient.sendMessageToServer("velocity", msg);
        }
        CommonClient.addMessageHandler(c -> {
            if (c.data.equals("shutdown")) {
                Bukkit.shutdown();
            }
        });
        Bukkit.getPluginManager().registerEvents(new BasicPointsListener(), this);
        SPECTATOR_TEAM = getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("spectators");
        SPECTATOR_TEAM.setAllowFriendlyFire(false);
        SPECTATOR_TEAM.setCanSeeFriendlyInvisibles(true);
        SPECTATOR_TEAM.color(NamedTextColor.GRAY);
        ownerUUID = Bukkit.getOfflinePlayer(ownerName).getUniqueId().toString();
        MINIBOSS_TEAM = getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("miniboss");
        MINIBOSS_TEAM.color(NamedTextColor.DARK_RED);
        Runtime.getRuntime().addShutdownHook(CommonClient.getShutdownHook());
    }

    @Override
    public void onDisable() {
        Database.updateScore();
    }

    public void tryActivate() {
        if (!world_ready || difficulty == null) {
            return;
        }
        var p = Bukkit.getPlayerExact(ownerName);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.MASTER, 1.0f, 1.0f);
        Bukkit.getServer().sendMessage(Component.text("Selected difficulty: ").append(difficulty.toText()));
        
        p.getWorld().getWorldBorder().setSize(400, 10);
        difficulty.activate();
        for (Component c : DifficultyMenu.createDesc(difficulty)) {
            Bukkit.getServer().sendMessage(c);
        }
        p.setInvulnerable(false);
        p.removePotionEffect(PotionEffectType.SATURATION);
        p.setGameMode(GameMode.SURVIVAL);
        Bukkit.getScheduler().runTaskLater(HardcorePlugin.instance, () -> {
            p.getWorld().getWorldBorder().setSize(320000);
        }, 10 * 20);
    }
}
