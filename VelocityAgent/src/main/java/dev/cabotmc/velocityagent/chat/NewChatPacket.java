package dev.cabotmc.velocityagent.chat;

import java.time.Instant;
import java.util.UUID;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;

import dev.simplix.protocolize.api.PacketDirection;
import dev.simplix.protocolize.api.packet.AbstractPacket;
import dev.simplix.protocolize.api.util.ProtocolUtil;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

public class NewChatPacket extends AbstractPacket {
    String message;
    Player sender;
    String plainText;
    public NewChatPacket() {}

    public NewChatPacket(Component text, Player sender, String unformatted) {
        //message = ProtocolUtils.getJsonChatSerializer(ProtocolVersion.MINECRAFT_1_19_1).serialize(text);
        message = "{\"text\": \"[Test Message]\"}";
        plainText = unformatted;
        this.sender = sender;
    }
    @Override
    public void read(ByteBuf arg0, PacketDirection arg1, int arg2) {
        // dont matetr
        
    }
    @Override
    public void write(ByteBuf buf, PacketDirection direction, int protocolVersion) {
        // write chat format
        // write false - no message signature
        buf.writeByte((byte) 0);
        // write player UUID
        var id = sender.getUniqueId();
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
        // write 0 for 0 length byte array
        ProtocolUtil.writeVarInt(buf, 0);

        // write message body
        ProtocolUtil.writeString(buf, plainText);
        // write boolean true - formatted message present
        buf.writeByte((byte) 1);
        ProtocolUtil.writeString(buf, message);
        // write timestamp
        buf.writeLong(Instant.now().toEpochMilli());
        // write salt
        buf.writeLong(0);
        // write no previous messages
        ProtocolUtil.writeVarInt(buf, 0);

        // write true - unsigned content present
        buf.writeByte((byte) 0);
        // write unsigned content
       // ProtocolUtil.writeString(buf, message);
        // write filter type - PASS_THROUGH
        ProtocolUtil.writeVarInt(buf, 0);
        
        //write network info
        // chat type 0 - chat message
        ProtocolUtil.writeVarInt(buf, 0);
        // write sender's name as component i think? maybe try uuid
        //ProtocolUtils.writeString(buf, ProtocolUtils.getJsonChatSerializer(ProtocolVersion.MINECRAFT_1_19_1).serialize(Component.text(sender.getUsername())));
        // write false - no target name
        //buf.writeByte((byte) 0);
        
        
    }
    
}
