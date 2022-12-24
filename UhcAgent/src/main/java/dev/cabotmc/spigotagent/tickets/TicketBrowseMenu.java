package dev.cabotmc.spigotagent.tickets;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import dev.cabotmc.spigotagent.SpigotAgent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TicketBrowseMenu implements Listener {
    static ArrayList<ItemCategory> categoryCache = new ArrayList<>();
    static {
        categoryCache.add(new ItemCategory(Material.BRICKS, CreativeCategory.BUILDING_BLOCKS));
        categoryCache.add(new ItemCategory(Material.PEONY, CreativeCategory.DECORATIONS));
        categoryCache.add(new ItemCategory(Material.REDSTONE, CreativeCategory.REDSTONE));
        categoryCache.add(new ItemCategory(Material.POWERED_RAIL, CreativeCategory.TRANSPORTATION));
        categoryCache.add(new ItemCategory(Material.LAVA_BUCKET, CreativeCategory.MISC));
        categoryCache.add(new ItemCategory(Material.APPLE, CreativeCategory.FOOD));
        categoryCache.add(new ItemCategory(Material.IRON_AXE, CreativeCategory.TOOLS));
        categoryCache.add(new ItemCategory(Material.GOLDEN_SWORD, CreativeCategory.COMBAT));
        categoryCache.add(new ItemCategory(Material.POTION, CreativeCategory.BREWING));
    }
    ItemCategory selectedCategory = categoryCache.get(0);
    HashMap<Integer, Material> buttons = new HashMap<>();
    ItemStack UP_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemStack DOWN_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    Player p;
    Inventory i;
    int pageOffset = 0;
    boolean returnBlankItem;

    public TicketBrowseMenu(Player target, boolean returnIfBlank) {
        var m = UP_ITEM.getItemMeta();
        m.displayName(Component.text("Row Up", TextColor.color(0x4cba8f)).decoration(TextDecoration.ITALIC, false));
        UP_ITEM.setItemMeta(m);
        m = DOWN_ITEM.getItemMeta();
        m.displayName(Component.text("Row Down", TextColor.color(0xba4c4c)).decoration(TextDecoration.ITALIC, false));
        DOWN_ITEM.setItemMeta(m);
        p = target;
        returnBlankItem = returnIfBlank;
        i = Bukkit.createInventory(null, 54, Component.text("Pick an Item"));
        Bukkit.getPluginManager().registerEvents(this, SpigotAgent.instance);
        render();
    }

    public void open() {
        p.openInventory(i);
    }

    public void render() {
        buttons.clear();
        i.clear();
        int slotIndex = 0;
        for (var c : categoryCache) {
            i.setItem(slotIndex, c.getIcon(selectedCategory.equals(c)));
            slotIndex++;
        }
        for (int j = 0; j < 9; j++) {
            i.setItem(slotIndex, UP_ITEM);
            slotIndex++;

        }
        for (var x : selectedCategory.renderPage(pageOffset)) {
            i.setItem(slotIndex, x);
            buttons.put(slotIndex, x.getType());
            slotIndex++;
        }
        slotIndex = 54 - 9;
        for (int j = 0; j < 9; j++) {
            i.setItem(slotIndex, DOWN_ITEM);
            slotIndex++;
        }
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!e.getInventory().equals(i))
            return;
        e.setCancelled(true);
        if (!e.getClickedInventory().equals(i))
            return;
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f);

        if (e.getSlot() < 9) {
            // choose creative type
            selectedCategory = categoryCache.get(e.getSlot());
            pageOffset = 0;
            render();
        } else if (e.getSlot() < 18) {
            // move page up
            pageOffset = pageOffset - 1;
            if (pageOffset < 0)
                pageOffset = 0;
            render();
        } else if (e.getSlot() < 45) {
            if (!buttons.containsKey(e.getSlot())) {
                pageOffset = pageOffset + 1;
                if (!selectedCategory.canRender(pageOffset))
                    pageOffset = pageOffset - 1;
                render();
                return;
            }
            // choose item
            var clickedItem = buttons.get(e.getSlot());
            returnBlankItem = false;
            TicketUtil.giveFilledTicketToPlayer(p, clickedItem);
            p.closeInventory();
        } else {
            // down page
            pageOffset = pageOffset + 1;
            if (!selectedCategory.canRender(pageOffset))
                pageOffset = pageOffset - 1;
            render();
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (!e.getInventory().equals(i))
            return;
        if (returnBlankItem) {
            TicketUtil.giveBlankTicketToPlayer(p);
        }
        HandlerList.unregisterAll(this);
    }

    static class ItemCategory {
        ItemStack icon;
        ArrayList<ItemStack> mats = new ArrayList<>();
        CreativeCategory category;

        public ItemCategory(Material m, CreativeCategory c) {
            this.icon = new ItemStack(m);
            category = c;
            var im = icon.getItemMeta();
            im.displayName(
                    Component.text(c.name(), TextColor.color(0xeddf1f))
                            .decoration(TextDecoration.ITALIC, false));
            icon.setItemMeta(im);
        }

        public boolean acceptsItem(Material m) {
            return m.getCreativeCategory() != null && m.getCreativeCategory().equals(category);
        }

        public ItemStack getIcon(boolean glowing) {
            if (glowing) {
                var i = icon.clone();
                var m = i.getItemMeta();
                m.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                i.setItemMeta(m);
                return i;
            } else {
                return this.icon;
            }
        }

        public void addItem(Material m) {
            mats.add(new ItemStack(m));
        }

        public boolean canRender(int startLine) {
            return startLine * 9 < mats.size();
        }

        public ArrayList<ItemStack> renderPage(int startLine) {
            if (startLine * 9 >= mats.size()) {
                return new ArrayList<>();
            } else {
                int max = Math.min(27, mats.size() - (startLine * 9));
                int offset = startLine * 9;
                var arr = new ArrayList<ItemStack>();
                for (int i = 0; i < max; i++) {
                    arr.add(mats.get(offset + i));
                }
                return arr;
            }
        }

        public boolean equals(ItemCategory other) {
            return other.category.equals(this.category);
        }
    }

    public static void initItems() {
        for (var m : Material.values()) {
            if (m.isLegacy())
                continue;
            CreativeCategory c;
            try {
                c = m.getCreativeCategory();
            } catch (Exception e) {
                continue;
            }
            if (c == null || !m.isItem())
                continue;
            for (var x : categoryCache) {
                if (x.acceptsItem(m) && !m.toString().contains("SPAWN_EGG")) {
                    x.addItem(m);
                    break;
                }
            }
        }
    }
}
