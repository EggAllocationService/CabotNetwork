package dev.cabotmc.spigotagent;

import dev.cabotmc.spigotagent.beacon.BeaconListener;
import dev.cabotmc.spigotagent.rules.LimitedTotemRule;
import dev.cabotmc.spigotagent.tickets.TicketBrowseMenu;
import dev.cabotmc.spigotagent.tickets.TicketListener;
import dev.cabotmc.spigotagent.tickets.TicketUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.val59000mc.customitems.Kit;
import com.gmail.val59000mc.customitems.KitsManager;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public final class SpigotAgent extends JavaPlugin {
    public static SpigotAgent instance;
    public static Jedis jedis;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new TimeListener(), this);
        Bukkit.getPluginManager().registerEvents(new WitherListener(), this);
        Bukkit.getPluginManager().registerEvents(new TicketListener(), this);
        Bukkit.getPluginManager().registerEvents(new BorderListener(), this);

        Bukkit.getPluginManager().registerEvents(new BeaconListener(), this);
        Bukkit.getPluginManager().registerEvents(new LimitedTotemRule(), this);

        Bukkit.getWorld("world").getWorldBorder().setWarningTime(20);
        TicketBrowseMenu.initItems();
        TicketUtil.loadComputedJson();
        getCommand("debugticket").setExecutor(new DebugTicketCommand());
        // get fucked nerds
        try {
            var f = KitsManager.class.getDeclaredField("kits");
            f.setAccessible(true); // fuck you
            List<Kit> kits = (List<Kit>) f.get(null);
            var k = new Kit.Builder("tickets")
                .setName("Tickets")
                .setSymbol(createKitIcon())
                .addItem(createUnbreakable(Material.WOODEN_SWORD))
                .addItem(createUnbreakable(Material.WOODEN_PICKAXE))
                .addItem(createUnbreakable(Material.WOODEN_AXE))
                .addItem(createUnbreakable(Material.WOODEN_SHOVEL))
                .addItem(TicketUtil.createBlankTicket())
                .build();
            kits.add(k);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        jedis = new Jedis("redis");
        jedis.connect();
    }

    static ItemStack createKitIcon() {
        var i = new ItemStack(Material.PAPER);
        var m = i.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Tickets");
        var l = new ArrayList<Component>();
        l.add(Component.text("- Wooden tools").decoration(TextDecoration.ITALIC, false));
        l.add(Component.text("- Item Lookup Ticket").decoration(TextDecoration.ITALIC, false));
        m.lore(l);
        i.setItemMeta(m);
        return i;
    }

    @Override
    public void onDisable() {
        if (!System.getenv().containsKey("CABOT_NAME"))
            return;
    }
    private ItemStack createUnbreakable(Material m) {
        var i = new ItemStack(m);
        var e = i.getItemMeta();
        e.setUnbreakable(true);
        i.setItemMeta(e);
        return i;
    }

}
