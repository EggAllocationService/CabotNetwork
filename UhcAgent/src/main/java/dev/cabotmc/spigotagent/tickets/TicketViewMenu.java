package dev.cabotmc.spigotagent.tickets;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import dev.cabotmc.spigotagent.SpigotAgent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TicketViewMenu implements Listener {
    Player p;
    ArrayList<String> sources;
    Inventory i;
    public TicketViewMenu(Player target, Material m) {
        p = target;
        if (TicketUtil.relations.containsKey(m.getKey().toString())) {
            sources = TicketUtil.relations.get(m.getKey().toString());
            i = Bukkit.createInventory(null, 54, Component.text("Sources for " + m.getKey().toString()));
        } else {
            sources = new ArrayList<>();
            i = Bukkit.createInventory(null, 45, Component.text("Sources for " + m.getKey().toString()));
        }
        Bukkit.getPluginManager().registerEvents(this, SpigotAgent.instance);
        render();

    }
    public void open() {
        p.openInventory(i);
    }
    public void render() {
        if (sources.size() == 0) {
            // 22 is middle
            var v = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            var m = v.getItemMeta();
            m.displayName(Component.text("No sources found", TextColor.color(0xca0707)).decoration(TextDecoration.ITALIC, false));
            v.setItemMeta(m);
            i.setItem(22, v);
        } else {
            for (var source: sources) {
                var x = createItemStack(source);
                if (x != null) {
                    i.addItem(x);
                }
            }
        }
    }
    @EventHandler
    public void click(InventoryClickEvent e) {
        if (e.getInventory().equals(i)) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (e.getInventory().equals(i)) {
            HandlerList.unregisterAll(this);
        }
    }
    public ItemStack createItemStack(String source) {
        var m = Material.matchMaterial(source);
        if (m != null) {
            return new ItemStack(m);
        } else {
            if (source.equals("player")) return null;
            if (findEntityType(source) != null) {
                var i = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta d = (SkullMeta) i.getItemMeta();
                d.setOwner("MHF_" + convertNamespacedEntityToLegacy(source, false));
                d.displayName(Component.text(
                    convertNamespacedEntityToLegacy(source, true)
                    ).decoration(TextDecoration.ITALIC, false));
                i.setItemMeta(d);
                return i;
            } else {
                var i = new ItemStack(Material.CHEST);
                var d = i.getItemMeta();
        
                d.displayName(Component.text(
                    convertNamespacedEntityToLegacy(source, true),
                    TextColor.color(0xeddf1f)
                    ).decoration(TextDecoration.ITALIC, false));
                i.setItemMeta(d);
                return i;
            }
        }
    }
    static EntityType findEntityType(String name) {
        var n = name.toUpperCase();
        for (var x : EntityType.values()) {
            if(x.toString().equals(n)) return x;
        }
        return null;
    }
    static String convertNamespacedEntityToLegacy(String name, boolean addSpace) {
        var base = "";
        for (var x : name.split("_")) {
            base += x.substring(0, 1).toUpperCase() + x.substring(1);
            if (addSpace) base = base + " ";
        }
        return base;
    }
}
