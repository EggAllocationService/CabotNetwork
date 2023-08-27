package dev.cabotmc.spigotagent;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.pingsystem.api.PingAPI;
import dev.cabotmc.spigotagent.elo.EloListener;
import dev.cabotmc.spigotagent.elo.EloService;
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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.val59000mc.customitems.Kit;
import com.gmail.val59000mc.customitems.KitsManager;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SpigotAgent extends JavaPlugin {
    public static SpigotAgent instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!System.getenv().containsKey("CABOT_NAME")) return;
        try {
            CommonClient.delayQueuePacket(true);
            CommonClient.init();
            CommonClient.sayHello(Bukkit.getPort());
            CommonClient.sendMessageToServer("velocity", "queue:uhc:prep");
            CommonClient.addMessageHandler(msg -> {
                if (msg.data.equals("shutdown")) {
                    Bukkit.shutdown();
                }
            });
            Bukkit.getPluginManager().registerEvents(new TimeListener(), this);
            Bukkit.getPluginManager().registerEvents(new WitherListener(), this);
            Bukkit.getPluginManager().registerEvents(new TicketListener(), this);
            Bukkit.getPluginManager().registerEvents(new EloListener(), this);
            Bukkit.getPluginManager().registerEvents(new BorderListener(), this);
            EloService.init();
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                var first = Component.text("Cabot", TextColor.color(0x45abe6));
                var second = Component.text("AC", TextColor.color(0xe645e0));
                var third = Component.text(" | Strict mode (Δa=0.001)", TextColor.color(NamedTextColor.WHITE));
                Bukkit.getServer().sendActionBar(Component.join(JoinConfiguration.noSeparators(), first, second, third));
            }
            , 0, 10);
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
                    .addItem(new ItemStack(Material.WOODEN_SWORD))
                    .addItem(new ItemStack(Material.WOODEN_PICKAXE))
                    .addItem(new ItemStack(Material.WOODEN_AXE))
                    .addItem(new ItemStack(Material.WOODEN_SHOVEL))
                    .addItem(TicketUtil.createBlankTicket())
                    .build(); 
                kits.add(k);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            PingAPI.setPermissionSolver(p -> p.getGameMode() == GameMode.SURVIVAL);
            PingAPI.setVisibilitySolver(p -> {
                for (var t : GameManager.getGameManager().getTeamManager().getUhcTeams()) {
                    
                    var players = t.getMembers().stream()
                        .filter(c -> c.isOnline())
                        .map(c -> {
                            try {
                                return c.getPlayer();
                            } catch (UhcPlayerNotOnlineException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(c -> c != null)
                        .collect(Collectors.toCollection(() -> new ArrayList<>()));
                    if (players.contains(p)) {
                        return players;
                    }
                }
                var x = new ArrayList<Player>();
                x.add(p);
                return x;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


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
        CommonClient.getShutdownHook().run();
    }

}
