package dev.cabotmc.spigotagent;

import com.esotericsoftware.kryonet.Client;
import dev.cabotmc.mgmt.ProtocolHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;

public final class SpigotAgent extends JavaPlugin {
    public static Client kryoClient;

    @Override
    public void onEnable() {
        // Plugin startup logic
        kryoClient = new Client();
        ProtocolHelper.registerClasses(kryoClient.getKryo());
        kryoClient.start();
        try {
            kryoClient.connect(5000, "172.17.0.1", 3269);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents(new SpigotListener(), this);
    }

    @Override
    public void onDisable() {
        kryoClient.close();
    }
}
