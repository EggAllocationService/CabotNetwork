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
import dev.cabotmc.mgmt.containers.WrappedContainer;
import dev.cabotmc.mgmt.protocol.ClientIdentifyMessage;
import dev.cabotmc.mgmt.templates.FileBasedRegistry;
import dev.cabotmc.mgmt.templates.TemplateRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static Server server;
    public static DockerClient docker;

    static TemplateRegistry registry;

    public static void main(String[] args) throws IOException {
        DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        registry = new FileBasedRegistry(new File("/home/ubuntu/craftin-containers/templates"));

        ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
                .dockerHost(standard.getDockerHost())
                .build();
        docker = DockerClientImpl.getInstance(standard, httpClient);
        docker.pingCmd().exec();
        server = new Server();
        server.start();
        ProtocolHelper.registerClasses(server.getKryo());
        ContainerManager.loadRunningContainers();
        if (!ContainerManager.trackedContainers.containsKey("velocity")) {
            System.out.println("Starting velocity");
            ContainerManager.startContainerWithName(registry.getByName("velocity"), "velocity", null);
        }
        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof ClientIdentifyMessage) {
                    var message = (ClientIdentifyMessage) o;
                    System.out.println("Client " + message.instanceName + " identified as " + message.kind);
                    if (message.instanceName.equals("velocity")) {
                        System.out.println("Starting lobby container");
                        ContainerManager.startContainerWithName(registry.getByName("velocity"), "lobby", null);
                    }
                    if (ContainerManager.trackedContainers.containsKey(message.instanceName)) {
                        ContainerManager.trackedContainers.get(message.instanceName).containerConnection = connection;
                        
                    }
                } else {
                    ContainerCommunicationManager.acceptMessage(o, connection);
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        server.bind(3269);
    }
    public static class ShutdownThread extends Thread {
        @Override
        public void run() {
            var containers = ContainerManager.trackedContainers;
            var toDelete = new ArrayList<>(containers.values());
            for (var x : toDelete) {
                docker.stopContainerCmd(x.containerID).exec();
            }
            System.out.println("Stopped containers");
        }
    }
}