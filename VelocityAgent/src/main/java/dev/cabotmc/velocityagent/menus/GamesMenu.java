package dev.cabotmc.velocityagent.menus;

import java.util.ArrayList;
import java.util.HashMap;

import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.velocityagent.BaseMenu;
import dev.cabotmc.velocityagent.VelocityAgent;
import dev.cabotmc.velocityagent.queue.Queue;
import dev.cabotmc.velocityagent.queue.QueueManager;
import dev.simplix.protocolize.api.ClickType;
import dev.simplix.protocolize.api.SoundCategory;
import dev.simplix.protocolize.api.inventory.InventoryClick;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.Sound;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class GamesMenu extends BaseMenu{
    public GamesMenu() {
        super(InventoryType.GENERIC_9X6);
        inv.title(Component.text("Available Gamemodes"));
    }
    volatile HashMap<Integer, Queue> buttonLayout;
    volatile boolean hasLobbyButton = false;
    @Override
    public void render() {
        
    }
    @Override
    public void onOpen(Player p) {
        if (p.getCurrentServer().isPresent() && !p.getCurrentServer().get().getServerInfo().getName().equals("lobby")) {
            var lobbyItem = new ItemStack(ItemType.OAK_DOOR);
            lobbyItem.displayName(Component.text("Back to Lobby", TextColor.color(0xca0707)).decoration(TextDecoration.ITALIC, false));
            inv.item(4, lobbyItem);
            hasLobbyButton = true;
        }
        inv.items().clear();
        buttonLayout = new HashMap<>();
        int slot = 10;
        for (var q : QueueManager.getGameModes()) {
            if (!q.hasPermission(p)) continue;
            var i = q.createIcon();
            i.addToLore(Component.text(" "));
            i.addToLore(Component.text("Click to connect", TextColor.color(0xcaca07)).decoration(TextDecoration.ITALIC, false));
            buttonLayout.put(slot, q);
            inv.item(slot, i);
            slot++;
        }
    }

    @Override
    public void click(InventoryClick e) {
        var p = e.player();
        if (e.clickType() != ClickType.LEFT_CLICK) {
            return;
        }
        var slot = e.slot();
        if (buttonLayout.containsKey(slot)) {
            var q = buttonLayout.get(slot);
            p.closeInventory();
            q.addPlayer(VelocityAgent.getProxy().getPlayer(p.uniqueId()).get());
        } else if (e.slot() == 4 && hasLobbyButton) {
            p.closeInventory();
            var pp = VelocityAgent.getProxy().getPlayer(p.uniqueId()).get();
            var lobby = VelocityAgent.getProxy().getServer("lobby").get();
            pp.createConnectionRequest(lobby).fireAndForget();
        }
    }
    
}
