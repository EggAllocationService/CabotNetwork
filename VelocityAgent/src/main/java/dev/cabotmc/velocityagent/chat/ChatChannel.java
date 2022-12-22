package dev.cabotmc.velocityagent.chat;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.proxy.Player;

import dev.simplix.protocolize.api.item.ItemStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;

public abstract class ChatChannel implements ForwardingAudience {
    ArrayList<Player> members = new ArrayList<>();
    String name;
    public ChatChannel(String name) {
        this.name = name;
    }
    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return members;
    }
    abstract boolean canJoin(Player p);
    abstract boolean shouldAutoJoin(Player p);
    public void addPlayer(Player p) {
        members.add(p);
    }
    public void removePlayer(Player p) {
        members.remove(p);
    }
    abstract ItemStack makeIcon(Player p);
}
