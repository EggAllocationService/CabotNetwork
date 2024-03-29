package dev.cabotmc.mgmt.containers;

import com.esotericsoftware.kryonet.Connection;

import dev.cabotmc.mgmt.Main;
import dev.cabotmc.mgmt.protocol.CreateServerRequestMessage;
import dev.cabotmc.mgmt.protocol.CrossServerMessage;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;
import dev.cabotmc.mgmt.templates.TemplateRegistry;

public class ContainerCommunicationManager {
    public static void acceptMessage(Object o, Connection connection) {
        if (o instanceof ServerStatusChangeMessage) {
            var msg = (ServerStatusChangeMessage) o;

            if (!msg.online) {
                
                ContainerManager.trackedContainers.remove(msg.serverName);
                ContainerManager.trackedContainers.get("velocity").containerConnection.sendTCP(o);
            } else {
                if (ContainerManager.trackedContainers.containsKey(msg.serverName) && msg.connectAddress == null) {
                    var container = ContainerManager.trackedContainers.get(msg.serverName);
                    var info = Main.docker.inspectContainerCmd(container.containerID).exec();
                    msg.connectAddress = info.getNetworkSettings().getIpAddress();
                    System.out.println("Set connect address to " + msg.connectAddress);
                }
                ContainerManager.trackedContainers.get("velocity").containerConnection.sendTCP(msg);
            }
        } else if (o instanceof CreateServerRequestMessage) {
            var template = TemplateRegistry.templates.get(((CreateServerRequestMessage) o).templateName);
            ContainerManager.requestContainerStart(template, ((CreateServerRequestMessage) o).enviromentVars);
        } else if (o instanceof CrossServerMessage) {
            var msg = (CrossServerMessage) o;
            for (var target : msg.targets) {
                if (ContainerManager.trackedContainers.containsKey(target) && ContainerManager.trackedContainers.get(target).containerConnection != null) {
                    ContainerManager.trackedContainers.get(target).containerConnection.sendTCP(msg);
                } else if (target.equals("*")) {
                    for (var x :ContainerManager.trackedContainers.values()) {
                        if (!x.containerID.equals(msg.from)) {
                            if (x.containerConnection == null) continue;
                            x.containerConnection.sendTCP(msg);
                        }
                    }
                }
            }
        }
    }

}
