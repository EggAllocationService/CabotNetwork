package dev.cabotmc.mgmt;

import com.esotericsoftware.kryonet.Server;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

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
        server.bind(3269);
    }
}