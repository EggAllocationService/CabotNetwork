package dev.cabotmc.spigotagent;

import dev.cabotmc.commonnet.CommonClient;

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
            CommonClient.addMessageHandler(msg -> {
                if (msg.data.equals("shutdown")) {
                    Bukkit.shutdown();
                }
            });
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
