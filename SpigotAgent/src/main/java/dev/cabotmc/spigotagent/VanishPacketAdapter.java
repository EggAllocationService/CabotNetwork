package dev.cabotmc.spigotagent;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class VanishPacketAdapter extends PacketAdapter {

    public VanishPacketAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.TAB_COMPLETE);
        
    }
    @Override
    public void onPacketSending(PacketEvent e) {
        var packet = e.getPacket();
        
    }
    
}
