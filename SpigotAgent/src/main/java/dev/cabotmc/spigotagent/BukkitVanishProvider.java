package dev.cabotmc.spigotagent;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;

import dev.cabotmc.vanish.VanishPlatformProvider;

public class BukkitVanishProvider implements VanishPlatformProvider, Listener {
    Plugin plugin;
    public BukkitVanishProvider(Plugin p) {
        plugin = p;
    }
    @Override
    public boolean isOnline(UUID u) {
        return Bukkit.getPlayer(u) != null;
    }

    @Override
    public void vanishPlayer(UUID player) {
        var origPlayer = Bukkit.getPlayer(player);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player) && !p.hasPermission("vanish.see")) {
                p.hidePlayer(plugin, origPlayer);
            }   
        }
        origPlayer.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        for (Entity a : origPlayer.getNearbyEntities(32, 32, 32)) {
            if (a instanceof Mob) {
                var m = (Mob) a;
                if (m.getTarget() == null) continue;
                if (m.getTarget().equals(origPlayer)) {
                    m.setTarget(null);
                }
            }
        }
        
    }

    @Override
    public void unvanishPlayer(UUID player) {
        var origPlayer = Bukkit.getPlayer(player);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player) && !p.hasPermission("vanish.see")) {
                p.showPlayer(plugin, origPlayer);
            }   
        }
        origPlayer.setMetadata("vanished", new FixedMetadataValue(plugin, false));
    }
    
}
