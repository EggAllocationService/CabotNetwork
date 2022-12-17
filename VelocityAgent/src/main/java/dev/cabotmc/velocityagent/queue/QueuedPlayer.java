package dev.cabotmc.velocityagent.queue;

import java.util.UUID;

import net.kyori.adventure.bossbar.BossBar;

public class QueuedPlayer {
    public UUID id;
    public String token;
    public BossBar displayedBar;
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueuedPlayer)) return false;
        return id.equals(((QueuedPlayer) o).id);
    }
}
