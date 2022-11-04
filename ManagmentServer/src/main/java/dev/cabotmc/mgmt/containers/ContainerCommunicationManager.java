package dev.cabotmc.mgmt.containers;

import com.esotericsoftware.kryonet.Connection;

import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;

public class ContainerCommunicationManager {
    public static void acceptMessage(Object o, Connection connection) {
        if (o instanceof ServerStatusChangeMessage) {
            // this is a server that just started announcing it is ready to accept players
            // or, a server announcing it is shutting down
            // forward this to velocity
            ContainerManager.trackedContainers.get("velocity").containerConnection.sendTCP(o);
            var msg = ((ServerStatusChangeMessage) o);
            if (!msg.online) {
                // means a server is announcing it is shutting down
                ContainerManager.trackedContainers.remove(msg.serverName);
            }
        }
    }

}
