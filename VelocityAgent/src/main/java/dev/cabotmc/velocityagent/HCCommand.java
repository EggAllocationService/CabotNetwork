package dev.cabotmc.velocityagent;

import java.util.HashMap;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.mgmt.protocol.CreateServerRequestMessage;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class HCCommand implements SimpleCommand {
    static HashMap<Player, BossBar> waitingBars = new HashMap<>();
    @Override
    public void execute(Invocation invocation) {
        
        var msg = new CreateServerRequestMessage();
        msg.templateName = "solohc";
        var p = (Player) invocation.source();
        if (waitingBars.containsKey(p)) return;
        msg.enviromentVars = new String[]{"HC_OWNER=" + p.getUsername()};
        var bar = BossBar.bossBar(
            Component.text("[SOLO HC] Waiting for server...", TextColor.color(0x8AFCB0)), 
            0, Color.BLUE, Overlay.PROGRESS);
        waitingBars.put(p, bar);
        p.showBossBar(bar);
        VelocityAgent.kryoClient.sendTCP(msg);
        p.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f));
        p.sendMessage(Component.text("You are now in queue for a server"));
        
    }
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return true;
    }
}
