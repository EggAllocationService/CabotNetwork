package dev.cabotmc.minestom;

import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;

public class Biomes {
    public static Biome CHRISTMAS_BIOME = Biome.builder()
            .precipitation(Biome.Precipitation.NONE)
            .downfall(1f)
            .temperature(0.15f)
            .category(Biome.Category.ICY)
            .name(NamespaceID.from("egg", "departmasland"))
            .build();
}
