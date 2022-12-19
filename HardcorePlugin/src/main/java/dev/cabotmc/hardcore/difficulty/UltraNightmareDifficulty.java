package dev.cabotmc.hardcore.difficulty;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
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
        multiplier = 18d;
    }

    @Override
    public ArrayList<Component> getInfo() {
        var a = super.getInfo();
        a.add(Component.text("Creepers have a 0.5s fuse time and move quickly", TextColor.color(0x8a0b0b)));
        a.add(Component.text("All mobs have 2x speed and 2x attack speed modifiers", TextColor.color(0x8a0b0b)));
        a.add(Component.text("Your F3 menu does not show coordinates", TextColor.color(0x8a0b0b)));
        a.add(Component.text("You have no invulnerability after taking damage", TextColor.color(0x8a0b0b)));
        a.add(Component.text("Taking any damage will randomly rotate your camera", TextColor.color(0x8a0b0b)));
        a.add(Component.text("Mobs will only target the player", TextColor.color(0x8a0b0b)));
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
        p.setMaximumNoDamageTicks(1);
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(8.0d);
        p.setAbsorptionAmount(4);
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
            if (!(e.getEntity() instanceof LivingEntity)) {
                LivingEntity l = (LivingEntity) e.getEntity();
                l.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("Creepey", 1, Operation.MULTIPLY_SCALAR_1));
                if (l.getAttribute(Attribute.GENERIC_ATTACK_SPEED) != null) {
                    l.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(new AttributeModifier("Creepey", 1, Operation.MULTIPLY_SCALAR_1));
                }
                if (l.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                    l.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(new AttributeModifier("Creepey", 0.5, Operation.MULTIPLY_SCALAR_1));
                }
            }
            
            if (e.getEntityType() == EntityType.CREEPER) {
                Creeper c = (Creeper) e.getEntity();
                c.setMaxFuseTicks(5);
                
            }
        }
        @EventHandler
        public void damage(EntityDamageEvent e) {
             if (e.getFinalDamage() == 0) return; 
             if (e.getEntityType() != EntityType.PLAYER) return;
             var p = (Player) e.getEntity();
             var l = p.getLocation();
             var pitch = (Math.random() * 40) - 20;
             l.setPitch(l.getPitch() + (float) pitch);
             var yaw = (Math.random() * 80) - 40;
             l.setYaw(l.getYaw() + (float) yaw);
             p.teleport(l);
        }
        @EventHandler(priority = EventPriority.LOWEST)
        public void target(EntityTargetEvent e) {
            if (e.getEntity() instanceof Mob && e.getTarget() != null) {
                if (!(e.getTarget() instanceof Player)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
