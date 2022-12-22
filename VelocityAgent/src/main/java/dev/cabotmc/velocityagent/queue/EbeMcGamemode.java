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

public class EbeMcGamemode extends AbstractSurvivalGamemode {
    
    public EbeMcGamemode() {
        super("ebemc", 0x23e885, BossBar.Color.GREEN);
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
