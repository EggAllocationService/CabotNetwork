package org.example;

import java.util.HashMap;

import dev.cabotmc.minestom.world.WorldProperties;
import dev.cabotmc.minestom.world.ZipFileChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.world.DimensionType;

public class InstanceTracker {
    static InstanceManager manager;
    static HashMap<String, InstanceContainer> instances = new HashMap<>();
    static HashMap<String, WorldProperties> worldProps = new HashMap<>();
    public static void init(InstanceManager i) {
        manager = i;

    }
    public static InstanceContainer create(String name) {
        return create(name, DimensionType.OVERWORLD);
    }
    public static InstanceContainer create(String name, DimensionType dim) {
        var f = manager.createInstanceContainer(dim);
        WorldProperties props;
        if (!name.equals("lobby")) {
            var loader = new ZipFileChunkLoader(name + ".egg");
            f.setChunkLoader(loader);
            props = loader.getProperties();
        } else {
            f.setGenerator(new FlatWorldGenerator());
            props = new WorldProperties();
        }
        instances.put(name, f);
        worldProps.put(name, props);
        f.getWorldBorder().setCenter(props.borderCenterX, props.borderCenterZ);
        f.getWorldBorder().setDiameter(props.borderRadius);
        return f;
    }
    public static InstanceContainer get(String name) {
        return instances.get(name);
    }
} 
