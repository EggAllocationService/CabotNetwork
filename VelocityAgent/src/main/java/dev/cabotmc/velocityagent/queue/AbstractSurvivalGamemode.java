package dev.cabotmc.velocityagent.queue;

import java.util.ArrayList;

import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.velocityagent.VelocityAgent;
import dev.simplix.protocolize.api.item.ItemStack;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public abstract class AbstractSurvivalGamemode extends Queue {
    ArrayList<Player> players = new ArrayList<>();
    BossBar queuedBar;
    int COLOR;
    boolean serverStarting = false;
    public AbstractSurvivalGamemode(String name, int color, BossBar.Color bColor) {
        super(name);
        COLOR = color;
        queuedBar = BossBar.bossBar(
            Component.text("[Queue] Waiting for server to start", TextColor.color(COLOR)), 0, bColor, Overlay.PROGRESS);
    }
    @Override
    public void addPlayer(Player p) {
        var server = VelocityAgent.getProxy().getServer(getName());
        if (!server.isPresent()) {
            // start server in background
            if (!serverStarting) {
                requestCreateServer(getName());
                serverStarting = true;
            }
            p.showBossBar(queuedBar);
            p.sendMessage(Component.text("Starting the server \"" + getName() + "\", please wait...", TextColor.color(COLOR)));
            players.add(p);
        } else {
            p.createConnectionRequest(server.get()).fireAndForget();
        }
    }

    @Override
    public void removePlayer(Player p) {
        players.remove(p);
        p.hideBossBar(queuedBar);
        
    }

    @Override
    public void onServerCreate(String serverName, String token) {
        var server = VelocityAgent.getProxy().getServer(getName()).get();
        for (var p : players) {
            p.sendMessage(Component.text("Transferring you to " + getName() + "...", TextColor.color(COLOR)));
            p.hideBossBar(queuedBar);
            p.createConnectionRequest(server).fireAndForget();
        }
        players.clear();
        serverStarting = false;
    }

    @Override
    public boolean isInQueue(Player p) {
        return players.contains(p);
    }
    
    public abstract ItemStack createIcon();
}
