package dev.cabotmc.hardcore;


import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.hardcore.points.BasicPointsListener;
import dev.cabotmc.hardcore.points.PointsManager;
import dev.cabotmc.mgmt.protocol.CrossServerMessage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class HardcorePlugin extends JavaPlugin {
    public static String ownerName;
    public static HardcorePlugin instance;
    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new BasicListener(), this);
        ownerName = (String) System.getenv().getOrDefault("HC_OWNER", "ThatOneGamer999");
        getLogger().info("Set owner to " + ownerName);
        PointsManager.init();
        if (System.getenv().containsKey("CABOT_NAME")) {
            try {
                CommonClient.init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CommonClient.sayHello(Bukkit.getPort());
            var msg = "hcready:" + ownerName + ":" + System.getenv("CABOT_NAME");
            CommonClient.sendMessageToServer("velocity", msg);
        }
        Bukkit.getPluginManager().registerEvents(new BasicPointsListener(), this);
        
        
    }

    @Override
    public void onDisable() {
        CommonClient.getShutdownHook().run();
    }
}
