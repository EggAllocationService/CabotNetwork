package dev.cabotmc.hardcore.difficulty;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import dev.cabotmc.hardcore.HardcorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class UltraNightmareDifficulty extends NightmareDifficulty {
    public UltraNightmareDifficulty() {
        super();
        color = 0x8a0b0b;
        name = "Ultra Nightmare";
        displayMat = Material.WITHER_SKELETON_SKULL;
        multiplier = 7.8d;
    }

    @Override
    public ArrayList<Component> getInfo() {
        var a = super.getInfo();
        a.add(Component.text("Creepers have a 0.5s fuse time", TextColor.color(0x8a0b0b)));
        a.add(Component.text("Your F3 menu does not show coordinates", TextColor.color(0x8a0b0b)));
        a.add(Component.text("You have less invulnerability after taking damage", TextColor.color(0x8a0b0b)));
        a.add(Component.text("Your maximum health is four hearts", TextColor.color(0x8a0b0b)));
        //a.add(Component.text("You have some temporary health to shield you when you start", TextColor.color(0x8a0b0b)));

        a.add(Component.text("Good luck.", TextColor.color(0x8a0b0b)).decorate(TextDecoration.BOLD));
        return a;
    }
    @Override
    public Component toText() {
        var obfText = Component.text("AA", TextColor.color(color)).decorate(TextDecoration.BOLD).decorate(TextDecoration.OBFUSCATED);
        return obfText
            .append(Component.text(" Ultra Nightmare ", TextColor.color(color)).decorate(TextDecoration.BOLD).decoration(TextDecoration.OBFUSCATED, false))
            .append(obfText);
    }
    @Override
    public void activate() {
        super.activate();
        var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
        p.setMaximumNoDamageTicks(5);
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(8.0d);
        p.setAbsorptionAmount(6);
        var name = Component.text("Lucky Bed")
            .color(TextColor.color(0xff0f0f))
            .decoration(TextDecoration.ITALIC, false);
        var i = new ItemStack(Material.RED_BED);
        var meta = i.getItemMeta();
        meta.displayName(name);
        i.setItemMeta(meta);
        p.getInventory().addItem(i);
        for (var w : Bukkit.getWorlds()) {
            w.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
        }
        Bukkit.getPluginManager().registerEvents(new UltraModifiers(), HardcorePlugin.instance);
    }
    public static class UltraModifiers implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void spawn(EntitySpawnEvent e) {
            if (e.isCancelled()) return;
            if (e.getEntityType() == EntityType.CREEPER) {
                Creeper c = (Creeper) e.getEntity();
                c.setMaxFuseTicks(10);
            }
        }
    }
}
