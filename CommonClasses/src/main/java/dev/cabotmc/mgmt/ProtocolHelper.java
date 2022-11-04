package dev.cabotmc.mgmt;
import com.esotericsoftware.kryo.Kryo;
import dev.cabotmc.mgmt.protocol.CreateServerRequestMessage;
import dev.cabotmc.mgmt.protocol.CrossServerMessage;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;
import dev.cabotmc.mgmt.protocol.TransferPlayersMessage;

public class ProtocolHelper {
    public static void registerClasses(Kryo kryo) {
        kryo.register(ServerStatusChangeMessage.class);
        kryo.register(TransferPlayersMessage.class);
        kryo.register(CreateServerRequestMessage.class);
        kryo.register(CrossServerMessage.class);
    }
}