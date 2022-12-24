package dev.cabotmc.spigotagent.tickets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.google.gson.Gson;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TicketUtil {
    public static HashMap<String, ArrayList<String>> relations = new HashMap<>();
    public static void loadComputedJson() {
        try {
            var s = java.nio.file.Files.readString(Path.of("computed.json"));
            var g = new Gson();
            relations = g.fromJson(s, relations.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static NamespacedKey TICKET_KEY = new NamespacedKey("cabot", "ticket_type");
    public static NamespacedKey ITEM_KEY = new NamespacedKey("cabot", "ticket_item");
    public static NamespacedKey TICKED_ID = new NamespacedKey("cabot", "ticket_id");
    public static void giveBlankTicketToPlayer(Player p) {
        var i = createBlankTicket();
        if (p.getInventory().addItem(i).size() != 0) {
            p.getWorld().dropItem(p.getLocation(), i);
        }
    }
    public static ItemStack createBlankTicket() {
        var i = new ItemStack(Material.PAPER);
        var m = i.getItemMeta();
        m.displayName(
            Component.text("Discovery Voucher", TextColor.color(0xeddf1f))
            .decoration(TextDecoration.ITALIC, false)
        );
        var lore = new ArrayList<Component>();
        lore.add(
            Component.text(
                "Using this item will let you pick an item to view the potential sources of",
                TextColor.color(0x4c4c4c)
            ).decoration(TextDecoration.ITALIC, false)
        );
        lore.add(
            Component.text(
                "This action will consume the ticket. Choose wisely!",
                TextColor.color(0x4c4c4c)
            ).decoration(TextDecoration.ITALIC, false)
        );
        m.lore(lore);
        m.getPersistentDataContainer().set(TICKET_KEY, PersistentDataType.BYTE, (byte) 1);
        m.getPersistentDataContainer().set(TICKED_ID, PersistentDataType.INTEGER, ThreadLocalRandom.current().nextInt(50000));
        i.setItemMeta(m);
        return i;
    }
    public static void giveFilledTicketToPlayer(Player p, Material target) {
        var i = new ItemStack(Material.PAPER);
        var m = i.getItemMeta();
        m.displayName(
            Component.text("Filled Discovery Voucher", TextColor.color(0xeddf1f))
            .decoration(TextDecoration.ITALIC, false)
        );
        var lore = new ArrayList<Component>();
        lore.add(
            Component.text(
                "Using this item will let you view the potential sources of: ",
                TextColor.color(0x4c4c4c)
            ).decoration(TextDecoration.ITALIC, false)
        );
        lore.add(
            Component.text(
                target.name(),
                TextColor.color(0xeddf1f)
            ).decoration(TextDecoration.ITALIC, false)
        );
        m.lore(lore);
        m.getPersistentDataContainer().set(TICKET_KEY, PersistentDataType.BYTE, (byte) 2);
        m.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, target.getKey().toString());
        i.setItemMeta(m);
        if (p.getInventory().addItem(i).size() != 0) {
            p.getWorld().dropItem(p.getLocation(), i);
        }
    }

}
