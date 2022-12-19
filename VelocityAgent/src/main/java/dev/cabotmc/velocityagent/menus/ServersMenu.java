package dev.cabotmc.velocityagent.menus;

import java.util.HashMap;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import dev.cabotmc.velocityagent.BaseMenu;
import dev.cabotmc.velocityagent.VelocityAgent;
import dev.cabotmc.velocityagent.queue.QueueManager;
import dev.simplix.protocolize.api.ClickType;
import dev.simplix.protocolize.api.inventory.InventoryClick;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ServersMenu extends BaseMenu {
    volatile HashMap<Integer, String> servers = new HashMap<>();

    public ServersMenu() {
        super(InventoryType.GENERIC_9X6);
        inv.title(Component.text("Available Servers"));
    }

    @Override
    public void render() {
       
    }
    @Override
    public void onOpen(Player p) {
        var slot = 0;
        for (var v : VelocityAgent.getProxy().getAllServers()) {
            var name = v.getServerInfo().getName();
            if (name.equals("limbo") || name.equals("lobby")) continue;
            var handler = QueueManager.getGameMode(v.getServerInfo().getName().split("-")[0]);
            ItemStack base = null;
            var choseHandler = false;
            if (handler != null) {
                if (!handler.hasPermission(p)) {
                    // player doesnt have permission to join this type of game
                    continue;
                }
                base = handler.createServerIcon(v);
                choseHandler = true;
            }
            if (base == null) {
                base = createDefaultIcon(v);
            }
            if(choseHandler) {
                base.addToLore(Component.text(v.getServerInfo().getName(), TextColor.color(0x2e2e2e)).decoration(TextDecoration.ITALIC, false));
            }
            base.addToLore(Component.text(" "));
            base.addToLore(Component.text("Click to connect", TextColor.color(0xcaca07)).decoration(TextDecoration.ITALIC, false));
            inv.item(slot, base);
            servers.put(slot, v.getServerInfo().getName());
            slot++;
        }
        if (slot == 0) {
            var noItem = new ItemStack(ItemType.RED_STAINED_GLASS_PANE);
            noItem.displayName(
                Component.text("No servers available", TextColor.color(0xb70707))
                    .decoration(TextDecoration.ITALIC, false)
            );
            inv.type(InventoryType.GENERIC_9X5);
            inv.item(22, noItem);
        }
    }

    @Override
    public void click(InventoryClick e) {
        if (e.clickType() != ClickType.LEFT_CLICK) return;
        var slot = e.slot();
        if (!servers.containsKey(slot)) return;
        var target = servers.get(slot);
        var srv = VelocityAgent.getProxy().getServer(target);
        e.player().closeInventory();
        var p = VelocityAgent.getProxy().getPlayer(e.player().uniqueId()).get();
        if (srv.isPresent()) {
            p.createConnectionRequest(srv.get()).fireAndForget();
        } else {
            var msg = Component.text("Could connect you to the requested server", TextColor.color(0xe70707));
            p.sendMessage(msg);
        }
    }
    public ItemStack createDefaultIcon(RegisteredServer i) {
        var d = new ItemStack(ItemType.GRASS_BLOCK);
        d.displayName(Component.text(i.getServerInfo().getName(), TextColor.color(0xa7a707)).decoration(TextDecoration.ITALIC, false));
        return d;
    }

}
