package dev.cabotmc.spigotagent;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.pingsystem.api.PingAPI;
import dev.cabotmc.spigotagent.tickets.TicketBrowseMenu;
import dev.cabotmc.spigotagent.tickets.TicketListener;
import dev.cabotmc.spigotagent.tickets.TicketUtil;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class SpigotAgent extends JavaPlugin {
    public static SpigotAgent instance;
    @Override
    public void onEnable() {
        instance = this;
        if (!System.getenv().containsKey("CABOT_NAME")) return;
        try {
            CommonClient.delayQueuePacket(true);
            CommonClient.init();
            CommonClient.sayHello(Bukkit.getPort());
            CommonClient.sendMessageToServer("velocity", "queue:uhc:prep");
            CommonClient.addMessageHandler(msg -> {
                if (msg.data.equals("shutdown")) {
                    Bukkit.shutdown();
                }
            });
            Bukkit.getPluginManager().registerEvents(new TimeListener(), this);
            Bukkit.getPluginManager().registerEvents(new WitherListener(), this);
            Bukkit.getPluginManager().registerEvents(new TicketListener(), this);
            Bukkit.getWorld("world").getWorldBorder().setWarningTime(20);
            TicketBrowseMenu.initItems();
            TicketUtil.loadComputedJson();
            getCommand("debugticket").setExecutor(new DebugTicketCommand());
            PingAPI.setPermissionSolver(p -> p.getGameMode() == GameMode.SURVIVAL);
            PingAPI.setVisibilitySolver(p -> {
                for (var t : GameManager.getGameManager().getTeamManager().getUhcTeams()) {
                    var players = t.getMembers().stream()
                        .filter(c -> c.isOnline())
                        .map(c -> {
                            try {
                                return c.getPlayer();
                            } catch (UhcPlayerNotOnlineException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(c -> c != null)
                        .collect(Collectors.toCollection(() -> new ArrayList<>()));
                    if (players.contains(p)) {
                        return players;
                    }
                }
                var x = new ArrayList<Player>();
                x.add(p);
                return x;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        
    }

    @Override
    public void onDisable() {
        if (!System.getenv().containsKey("CABOT_NAME")) return;
        CommonClient.getShutdownHook().run();
    }
}
