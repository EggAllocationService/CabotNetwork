package dev.cabotmc.lobby.world;

import java.util.HashMap;

import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.world.DimensionType;

public class InstanceTracker {
    static InstanceManager manager;
    static HashMap<String, InstanceContainer> instances = new HashMap<>();
    public static void init(InstanceManager i) {
        manager = i;

    }
    public static InstanceContainer create(String name) {
        var f = manager.createInstanceContainer();
        f.setChunkLoader(new CustomChunkLoader());
        instances.put(name, f);
        return f;
    }
    public static InstanceContainer create(String name, DimensionType dim) {
        var f = manager.createInstanceContainer(dim);
        if (System.getenv().containsKey("CABOT_NAME") && !System.getenv().containsKey("IS_LIMBO")) {
            f.setChunkLoader(new ZipFileChunkLoader(name + ".egg"));
        } else {
            f.setChunkLoader(new CustomChunkLoader());
        }
        instances.put(name, f);
        return f;
    }
    public static InstanceContainer get(String name) {
        return instances.get(name);
    }
} 
