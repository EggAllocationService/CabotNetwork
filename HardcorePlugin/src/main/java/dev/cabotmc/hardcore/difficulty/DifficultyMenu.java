package dev.cabotmc.hardcore.difficulty;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.RandomDifficulty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class DifficultyMenu implements Listener {
    Inventory i;
    HashMap<Integer, BaseDifficulty> difficulties = new HashMap<>();
    boolean mayClose = false;
    boolean allowSpectators = true;
    
    public DifficultyMenu()  {
        i = Bukkit.createInventory(null, 54, Component.text("Choose a Difficulty"));
        difficulties.put(19, new EasyDifficulty());
        difficulties.put(22, new BaseDifficulty(Difficulty.NORMAL, "Medium", 0xd6b015, Material.ORANGE_STAINED_GLASS_PANE, 1.75));
        difficulties.put(25, new BaseDifficulty(Difficulty.HARD, "Hard", 0xe01422, Material.RED_STAINED_GLASS_PANE, 3));
        if (Math.random() > 0) {
            difficulties.put(38, new NightmareDifficulty());
            difficulties.put(40, new UltraNightmareDifficulty());
            difficulties.put(42, new RandomDifficulty());
        } else {
            difficulties.put(39, new NightmareDifficulty());
            difficulties.put(41, new UltraNightmareDifficulty());           
        }
        render();
    }
    public void open(Player p) {
        Bukkit.getPluginManager().registerEvents(this, HardcorePlugin.instance);
        p.openInventory(i);
    }
    public void render() {
        //i.clear();
        difficulties.forEach((place, diff) -> {
            i.setItem(place, createStack(diff));
        });
        i.setItem(53, createSpectatorItem());
    }
    public ItemStack createStack(BaseDifficulty difficulty) {
        difficulty.onRender();
        var i = new ItemStack(difficulty.displayMat);
        var meta = i.getItemMeta();
        meta.displayName(difficulty.toText().decoration(TextDecoration.ITALIC, false));
        meta.lore(createDesc(difficulty));
        i.setItemMeta(meta);
        return i;
    }
    public ItemStack createSpectatorItem() {
        var i = new ItemStack(allowSpectators ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        var meta = i.getItemMeta();
        if (allowSpectators) {
            meta.displayName(Component.text("Allowing Specatators", TextColor.color(0x27e627)).decoration(TextDecoration.ITALIC, false));
        } else {
            meta.displayName(Component.text("Disallowing Spectators", TextColor.color(0xe01422)).decoration(TextDecoration.ITALIC, false));
        }
        var a = new ArrayList<Component>();
        a.add(Component.text("Spectators may affect mob spawning characteristics.", TextColor.color(0xd9d025)).decoration(TextDecoration.ITALIC, false));
        a.add(Component.text("Disabling them will ensure vanilla spawning mechanics.", TextColor.color(0xd9d025)).decoration(TextDecoration.ITALIC, false));
        a.add(Component.text("Click to toggle", TextColor.color(0x27e627)).decoration(TextDecoration.ITALIC, false));
        meta.lore(a);
        i.setItemMeta(meta);
        return i;
    }
    public static ArrayList<Component> createDesc(BaseDifficulty difficulty) {
        var loreTmp = new ArrayList<Component>();
        for (var c : difficulty.getInfo()) {
            var x = Component.text(" - ", TextColor.color(0xd9d025)).decoration(TextDecoration.ITALIC, false);
            loreTmp.add(x.append(c.decoration(TextDecoration.ITALIC, false)).decoration(TextDecoration.ITALIC, false));
        }
        var mulBase = Component.text("Points Multiplier: ", TextColor.color(difficulty.color)).decoration(TextDecoration.ITALIC, false);
        mulBase = mulBase.append(Component.text(difficulty.getMultiplier() + "x", TextColor.color(0xf7ec19)).decoration(TextDecoration.ITALIC, false));
        loreTmp.add(mulBase);
        return loreTmp;
    }
    @EventHandler
    public void clickInv(InventoryClickEvent e) {
        if (e.getInventory() != i) return;
        e.setCancelled(true);
        var p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        if (difficulties.containsKey(e.getSlot())) {
            // set slot
            HardcorePlugin.difficulty = difficulties.get(e.getSlot());
            HardcorePlugin.difficulty.finalize();
            mayClose = true;
            HardcorePlugin.allowSpectators = allowSpectators;
            for (Player pd : Bukkit.getOnlinePlayers()) {
                if (!pd.getName().equals(HardcorePlugin.ownerName) && !allowSpectators) {
                    pd.kick(Component.text("The owner of this server has disabled spectating"));
                }
            }
            if (!HardcorePlugin.world_ready) {
                var m = Component.text("Pre-generating the world for you, please wait...")
                    .color(NamedTextColor.YELLOW);
                Bukkit.getServer().broadcast(m);
            }
            HardcorePlugin.instance.tryActivate();
            e.getWhoClicked().closeInventory();
        } else if (e.getSlot() == 53) {
            allowSpectators = !allowSpectators;
            render();
        }
    }
    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (e.getInventory() == i && !mayClose) {
           Bukkit.getScheduler().scheduleSyncDelayedTask(HardcorePlugin.instance, () -> {
                e.getPlayer().openInventory(i);
           }, 2);
        } else if (e.getInventory() == i && mayClose) {
            HandlerList.unregisterAll(this);
        }
    }  

    int tickCount = 0;
    @EventHandler
    public void tick(ServerTickEndEvent e) {
        tickCount++;
        if (tickCount >= 10) {
            tickCount = 0;
        } else {
            return;
        }

        render();
    }
}
