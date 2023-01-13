package dev.cabotmc.hardcore.randomdiff.modifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class RandomDrops extends Modifier implements Listener {
    HashMap<Material, LootGenerator> drops = new HashMap<>();
    List<LootGenerator> generators;
    public RandomDrops() {
        super("Random Drops");
        generators = Arrays.asList(Material.values())
        .stream()
        .filter(m -> m.isItem() || m.isBlock())
        .map(ItemGenerator::new)
        .collect(Collectors.toCollection(() -> new ArrayList<>()));
        Arrays.asList(LootTables.values())
            .stream()
            .map(l -> new LootTableGenerator(l.getLootTable()))
            .forEach(generators::add);
        Collections.shuffle(generators);
    }
    @Override
    public Component generateDescription() {
        return Component.text("Every block drop is randomized", TextColor.color(Modifier.NEUTRAL_COLOR))
        .append(Component.text("\nSpawn with a set of stone tools", TextColor.color(Modifier.GOOD_COLOR)))
        .append(Component.text("\nGain +2 to your points modifier", TextColor.color(Modifier.GOOD_COLOR)));
    }

    @Override
    public void activate() {
        HardcorePlugin.difficulty.setMultiplier(HardcorePlugin.difficulty.getMultiplier() + 2);
        var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
        p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        p.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE));
        p.getInventory().addItem(new ItemStack(Material.STONE_AXE));
        p.getInventory().addItem(new ItemStack(Material.STONE_SHOVEL));
        p.getInventory().addItem(new ItemStack(Material.STONE_HOE));
        
    }
    @EventHandler
    public void mainDrop(BlockDropItemEvent e) {
        var toDrop = getOrGenerateDrop(e.getBlockState().getType()).generateItems(e.getBlock().getLocation());
        e.setCancelled(true);
        for (var item : toDrop) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
        }
    }

    @EventHandler
    public void otherDrop(BlockDestroyEvent e) {
        var x = getOrGenerateDrop(e.getBlock().getType());
        e.setWillDrop(false);
        
        for (var item : x.generateItems(e.getBlock().getLocation())) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
        }
    }
    public LootGenerator getOrGenerateDrop(Material broken) {
        if (drops.containsKey(broken)) {
            return drops.get(broken);
        } else {
            var newDrop = generators.remove(generators.size() - 1);
            drops.put(broken, newDrop);
            return newDrop;
        }

    }
    public static interface LootGenerator {
        public Collection<ItemStack> generateItems(Location l);
    }
    public static class ItemGenerator implements LootGenerator {
        ItemStack i;
        public ItemGenerator(Material m) {
            i = new ItemStack(m);
        }
        @Override
        public List<ItemStack> generateItems(Location l) {
            return Arrays.asList(i);
        }
    }
    public static class LootTableGenerator implements LootGenerator {
        Random r = new Random();
        LootTable l;
        public LootTableGenerator(LootTable l) {
            this.l = l;
        }
        @Override
        public Collection<ItemStack> generateItems(Location l) {
            var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
           try {
            return this.l.populateLoot(r, new LootContext.Builder(l)
            .lootedEntity(p)
            .killer(p)
            .build());
           } catch(Exception e) {
            return new ArrayList<>();
           }
        }
        
    }
}