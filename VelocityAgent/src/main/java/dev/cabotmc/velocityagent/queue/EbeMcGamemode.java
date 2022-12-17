package dev.cabotmc.velocityagent.queue;

import java.util.ArrayList;

import org.w3c.dom.Text;

import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.velocityagent.VelocityAgent;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class EbeMcGamemode extends Queue {
    ArrayList<Player> players = new ArrayList<>();
    BossBar queuedBar;
    static final int COLOR = 0x23e885;
    boolean serverStarting = false;
    public EbeMcGamemode() {
        super("ebemc");
        queuedBar = BossBar.bossBar(
            Component.text("[Queue] Waiting for server to start", TextColor.color(COLOR)), 0, BossBar.Color.GREEN, Overlay.PROGRESS);
    }

    @Override
    public void addPlayer(Player p) {
        var server = VelocityAgent.getProxy().getServer("ebemc");
        if (!server.isPresent()) {
            // start server in background
            if (!serverStarting) {
                requestCreateServer("ebemc");
                serverStarting = true;
            }
            p.showBossBar(queuedBar);
            p.sendMessage(Component.text("Starting the survival server, please wait..."));
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
        var server = VelocityAgent.getProxy().getServer("ebemc").get();
        for (var p : players) {
            p.sendMessage(Component.text("Transferring you to the server..."));
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

    @Override
    public ItemStack createIcon() {
        var i = new ItemStack(ItemType.GRASS_BLOCK);
        i.displayName(Component.text("Eve Minecraft Gaming", TextColor.color(COLOR)).decoration(TextDecoration.ITALIC, false));
        return i;
    }
    @Override
    public boolean hasPermission(Player p) {
        return p.hasPermission("gamemode.ebemc");
    }
    
}
