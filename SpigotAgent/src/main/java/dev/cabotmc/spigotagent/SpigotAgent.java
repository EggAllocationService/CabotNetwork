package dev.cabotmc.spigotagent;

import com.esotericsoftware.kryonet.Client;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.mgmt.ProtocolHelper;
import dev.cabotmc.mgmt.protocol.ServerStatusChangeMessage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class SpigotAgent extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!System.getenv().containsKey("CABOT_NAME")) return;
        try {
            CommonClient.init();
            CommonClient.sayHello(Bukkit.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public void onDisable() {
        if (!System.getenv().containsKey("CABOT_NAME")) return;
        CommonClient.getShutdownHook().run();
    }
}
