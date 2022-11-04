package dev.cabotmc.mgmt;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

import dev.cabotmc.mgmt.containers.ContainerCommunicationManager;
import dev.cabotmc.mgmt.containers.ContainerManager;
import dev.cabotmc.mgmt.protocol.ClientIdentifyMessage;
import dev.cabotmc.mgmt.templates.TemplateRegistry;

import java.io.File;
import java.io.IOException;

public class Main {
    public static Server server;
    public static DockerClient docker;
    public static void main(String[] args) throws IOException {
        DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
                .dockerHost(standard.getDockerHost())
                .build();
        docker = DockerClientImpl.getInstance(standard, httpClient);
        docker.pingCmd().exec();
        server = new Server();
        server.start();
        ProtocolHelper.registerClasses(server.getKryo());
        TemplateRegistry.initFromFolder(new File("/home/ubuntu/craftin-containers/templates"));
        ContainerManager.loadRunningContainers();
        if (!ContainerManager.trackedContainers.containsKey("velocity")) {
            System.out.println("Starting velocity");
            ContainerManager.startContainerWithName(TemplateRegistry.templates.get("velocity"), "velocity");
        }
        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof ClientIdentifyMessage) {
                    var message = (ClientIdentifyMessage) o;
                    System.out.println("Client " + message.instanceName + " identified as " + message.kind);
                    if (message.instanceName.equals("velocity")) {
                        System.out.println("Starting lobby container");
                        ContainerManager.startContainerWithName(TemplateRegistry.templates.get("lobby"), "lobby");
                    }
                    if (ContainerManager.trackedContainers.containsKey(message.instanceName)) {
                        ContainerManager.trackedContainers.get(message.instanceName).containerConnection = connection;
                        
                    }
                } else {
                    ContainerCommunicationManager.acceptMessage(o, connection);
                }
            }
        });
        server.bind(3269);
    }
}