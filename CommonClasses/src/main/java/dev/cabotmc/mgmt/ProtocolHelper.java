package dev.cabotmc.mgmt;
import com.esotericsoftware.kryo.Kryo;
import dev.cabotmc.mgmt.protocol.*;

public class ProtocolHelper {
    public static void registerClasses(Kryo kryo) {
        kryo.register(ServerStatusChangeMessage.class);
        kryo.register(TransferPlayersMessage.class);
        kryo.register(CreateServerRequestMessage.class);
        kryo.register(CrossServerMessage.class);
        kryo.register(ClientIdentifyMessage.class);
        kryo.register(String[].class);
    }
}