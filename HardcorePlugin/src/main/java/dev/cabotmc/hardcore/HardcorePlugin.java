package dev.cabotmc.hardcore;

import com.google.common.io.ByteStreams;
import dev.cabotmc.hardcore.difficulty.BaseDifficulty;
import dev.cabotmc.hardcore.difficulty.DifficultyMenu;
import dev.cabotmc.hardcore.points.BasicPointsListener;
import dev.cabotmc.hardcore.points.PointsManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.jspecify.annotations.NonNull;
import redis.clients.jedis.Jedis;

public final class HardcorePlugin extends JavaPlugin {
    public static String ownerName;
    public static String ownerUUID;
    public static HardcorePlugin instance;
    public static Team SPECTATOR_TEAM;
    public static boolean world_ready = false;
    public static Team MINIBOSS_TEAM;
    public static BaseDifficulty difficulty;
    public static boolean allowSpectators = true;
    public static ItemStack TELEPORT_STACK;
    public static Jedis jedis;

    @Override
    public void onEnable() {
        // remove teams
        getServer().getScoreboardManager().getMainScoreboard().getTeams().forEach(Team::unregister);
        instance = this;

        jedis = new Jedis("redis", 6379);
        jedis.connect();

        Bukkit.getPluginManager().registerEvents(new BasicListener(), this);
        ownerName = (String) System.getenv().getOrDefault("HC_OWNER", "EggAllocationSrv");
        getLogger().info("Set owner to " + ownerName);
        PointsManager.init();

        Bukkit.getPluginManager().registerEvents(new BasicPointsListener(), this);
        SPECTATOR_TEAM = getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("spectators");
        SPECTATOR_TEAM.setAllowFriendlyFire(false);
        SPECTATOR_TEAM.setCanSeeFriendlyInvisibles(true);
        SPECTATOR_TEAM.setOption(Option.COLLISION_RULE, OptionStatus.FOR_OWN_TEAM);
        SPECTATOR_TEAM.color(NamedTextColor.GRAY);
        ownerUUID = Bukkit.getOfflinePlayer(ownerName).getUniqueId().toString();
        MINIBOSS_TEAM = getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("miniboss");
        MINIBOSS_TEAM.color(NamedTextColor.DARK_RED);
        TELEPORT_STACK = new ItemStack(Material.PURPLE_DYE);
        var m = TELEPORT_STACK.getItemMeta();
        m.displayName(Component.text("Right click to Teleport to " + ownerName, TextColor.color(0xa229e3)).decoration(TextDecoration.ITALIC, false));
        m.getPersistentDataContainer().set(new NamespacedKey("cabot", "tpitem"), PersistentDataType.BYTE, (byte) 1);
        TELEPORT_STACK.setItemMeta(m);

        jedis.publish("send-player", System.getenv().get("CABOT_NAME") + "," + ownerName);
        //PingAPI.setPermissionSolver(p -> p.getGameMode() != GameMode.ADVENTURE);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(x -> {
            x.registrar().register("abandon", new BasicCommand() {
                @Override
                public void execute(CommandSourceStack commandSourceStack, String[] args) {
                    var pkt = ByteStreams.newDataOutput();
                    pkt.writeUTF("lobby");
                    var data = pkt.toByteArray();
                    for (var player : Bukkit.getOnlinePlayers()) {
                        player.sendPluginMessage(HardcorePlugin.instance, "cabot:connect", data);
                    }

                    Bukkit.getScheduler().scheduleSyncDelayedTask(HardcorePlugin.instance, Bukkit::shutdown, 40);
                }

                @Override
                public boolean canUse(@NonNull CommandSender sender) {
                    return sender.getName().equals(ownerName);
                }
            });
        }));
    }

    @Override
    public void onDisable() {
    }

    public void tryActivate() {
        if (!world_ready || difficulty == null) {
            return;
        }
        var p = Bukkit.getPlayerExact(ownerName);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.MASTER, 1.0f, 1.0f);
        Bukkit.getServer().sendMessage(Component.text("Selected difficulty: ").append(difficulty.toText()));
        
        p.getWorld().getWorldBorder().changeSize(400, 10 * 20);
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

        getServer().getMessenger().registerOutgoingPluginChannel(this, "cabot:connect");
    }
}
