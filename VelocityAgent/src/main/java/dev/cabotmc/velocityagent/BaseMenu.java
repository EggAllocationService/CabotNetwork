package dev.cabotmc.velocityagent;

import com.velocitypowered.api.proxy.Player;

import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.inventory.InventoryClick;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import dev.simplix.protocolize.data.inventory.InventoryType;

public abstract class BaseMenu {
    public Inventory inv;
    public BaseMenu(InventoryType t) {
        inv = new Inventory(t);
        render();
        inv.onClick(this::click0);
    }   
    public abstract void render();
    public void open(Player p) {
        ProtocolizePlayer pp = Protocolize.playerProvider().player(p.getUniqueId());
        onOpen(p);
        pp.openInventory(inv);

    }
    public void onOpen(Player p) {

    }
    public abstract void click(InventoryClick e);

    public void click0(InventoryClick e) {
        e.cancelled(true);
        click(e);
    }

}
