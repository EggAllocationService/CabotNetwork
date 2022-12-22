package dev.cabotmc.spigotagent;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.vanish.VanishManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import java.io.IOException;

public final class SpigotAgent extends JavaPlugin {
    public static ProtocolManager pm;
    public static BukkitVanishProvider v;
    @Override
    public void onEnable() {
        if (!System.getenv().containsKey("CABOT_NAME")) return;
        try {
            Database.init();
            v = new BukkitVanishProvider(this);
            pm = ProtocolLibrary.getProtocolManager();
            VanishManager.init(Database.vanished);
            VanishManager.startWatcher(v);
            Bukkit.getPluginManager().registerEvents(new VanishListener(), this);
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
