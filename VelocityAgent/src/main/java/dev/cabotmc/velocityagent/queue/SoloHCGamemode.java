package dev.cabotmc.velocityagent.queue;

import java.util.ArrayList;
import java.util.HashMap;

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

public class SoloHCGamemode extends Queue {
    public ArrayList<QueuedPlayer> players = new ArrayList<>();
    public static HashMap<String, String> playerCache = new HashMap<>();
    public SoloHCGamemode() {
        super("solohc");
    }

    @Override
    public void addPlayer(Player p) {
        QueuedPlayer target = null;
        for (var x : players) {
            if (x.id.equals(p.getUniqueId())) {
                target = x;
                break;
            }
        }
        if (target != null) return;
        target = new QueuedPlayer();
        target.id = p.getUniqueId();
        target.displayedBar = BossBar.bossBar(
            Component.text("[SOLO HC] Waiting for server...", TextColor.color(0x8AFCB0)), 
            0, Color.BLUE, Overlay.PROGRESS);
        p.showBossBar(target.displayedBar);
        target.token = requestCreateServer("solohc", new String[]{"HC_OWNER=" + p.getUsername()});
        players.add(target);
        p.sendMessage(Component.text("You are now in queue for a server"));
        
    }

    @Override
    public void removePlayer(Player p) {
        QueuedPlayer target = null;
        for (var x : players) {
            if (x.id.equals(p.getUniqueId())) {
                target = x;
                break;
            }
        }
        if (target == null) return;
        p.hideBossBar(target.displayedBar);
        players.remove(target);

    }

    @Override
    public void onServerCreate(String serverName, String token) {
        QueuedPlayer target = null;
        for (var x : players) {
            if (x.token.equals(token)) {
                target = x;
                break;
            }
        }
        var pd = VelocityAgent.getProxy().getPlayer(target.id);
        if (pd.isEmpty()) {
            var omsg = new CrossServerMessage();
            omsg.targets = new String[] { serverName };
            omsg.data = "kill";
            omsg.from = "velocity";
            VelocityAgent.kryoClient.sendTCP(omsg);
            players.remove(target);
            return;
        }
        var p = pd.get();
        var targetSrv = VelocityAgent.getProxy().getServer(serverName).get();
        p.createConnectionRequest(targetSrv).fireAndForget();
        p.hideBossBar(target.displayedBar);
        players.remove(target);
        p.sendMessage(Component.text("Transferring you to " + serverName));
        playerCache.put(serverName, p.getUsername());
        return;
    }

    @Override
    public boolean isInQueue(Player p) {
        for (var x : players) {
            if (x.id.equals(p.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack createIcon() {
        var i = new ItemStack(ItemType.SKELETON_SKULL);
        i.displayName(Component.text("Solo Hardcore").color(TextColor.color(0xca4040)).decoration(TextDecoration.ITALIC, false));
        return i;
    }
    @Override
    public ItemStack createServerIcon(RegisteredServer r) {
        var i = new ItemStack(ItemType.PLAYER_HEAD);
        var srvName = r.getServerInfo().getName();
        if (!playerCache.containsKey(srvName)) return null;
        i.displayName(Component.text("Solo HC - " + playerCache.get(srvName), TextColor.color(0xe7e707)).decoration(TextDecoration.ITALIC, false));
        i.nbtData().putString("SkullOwner", playerCache.get(srvName));
        return i;
    }

}
