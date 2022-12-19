package dev.cabotmc.velocityagent.queue;

import java.util.ArrayList;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import dev.cabotmc.mgmt.protocol.CrossServerMessage;
import dev.cabotmc.velocityagent.VelocityAgent;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class UhcQueue extends Queue {
    String badServer;
    String targetServer;
    ArrayList<Player> players = new ArrayList<>();
    BossBar waitingBar;
    boolean serverStarting = false;
    public UhcQueue() {
        super("uhc");
        waitingBar = BossBar.bossBar(
            Component.text("[UHC] Waiting for server start", TextColor.color(0xe6196e)), 0, Color.RED, Overlay.PROGRESS);
    }
    @Override
    public void addPlayer(Player p) {
        RegisteredServer found = null;
        for (var s : VelocityAgent.getProxy().getAllServers()) {
            if (s.getServerInfo().getName().startsWith("uhc") && !s.getServerInfo().getName().equals(badServer) && s.getPlayersConnected().size() != 0) {
                found = s;
                break;
            }
        }
        if (found == null) {
            if (players.contains(p)) {
                return;
            }
            if (!serverStarting) {
                requestCreateServer("uhc");
                serverStarting = true;
            }
            players.add(p);
            p.showBossBar(waitingBar);
            p.sendMessage(Component.text("You are now in queue for a server"));
        } else {
            // suitable server exists, transfer
            p.createConnectionRequest(found).fireAndForget();
        }
        
    }

    @Override
    public void removePlayer(Player p) {
        p.hideBossBar(waitingBar);
        
    }

    @Override
    public void onServerCreate(String serverName, String token) {
        if (players.isEmpty()) {
            var msg = new CrossServerMessage();
            msg.data = "kill";
            msg.from = "velocity";
            msg.targets = new String[]{serverName};
            VelocityAgent.kryoClient.sendTCP(msg);
        } else {
            var s = VelocityAgent.getProxy().getServer(serverName).get();
            serverStarting = false;
            for (Player p : players) {
                p.hideBossBar(waitingBar);
                p.createConnectionRequest(s).fireAndForget();
            }
            targetServer = null;
            players.clear();
            updateBar();
        }
        
    }

    @Override
    public boolean isInQueue(Player p) {
        
        return players.contains(p);
    }

    @Override
    public ItemStack createIcon() {
        var i = new ItemStack(ItemType.IRON_SWORD);
        i.displayName(Component.text("Random UHC", TextColor.color(0xe6196e)).decoration(TextDecoration.ITALIC, false));
        return i;
    }
    @Override
    public void onServerMessage(String server, String msg) {
        if (msg.contains("prep")) {
            this.targetServer = server;
            updateBar();
        } else if (msg.contains("done")) {
            badServer = server;
            targetServer = null;
            updateBar();
        }
    }
    public void updateBar() {
        if (targetServer == null) {
            waitingBar.name(Component.text("[UHC] Waiting for server start [1/1]", TextColor.color(0xe6196e)));
            waitingBar.progress(0f);
        } else {
            waitingBar.name(Component.text("[UHC] Generating world [2/2]", TextColor.color(0xe6196e)));
            waitingBar.progress(0.5f);
        }
    }
    
}
