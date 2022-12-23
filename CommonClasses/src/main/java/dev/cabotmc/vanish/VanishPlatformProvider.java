package dev.cabotmc.vanish;

import java.util.UUID;

public interface VanishPlatformProvider {
    public boolean isOnline(UUID u);
    public void vanishPlayer(UUID player);
    public void unvanishPlayer(UUID player);
    public void scheduleTaskSync(Runnable r);
}
