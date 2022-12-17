package dev.cabotmc.hardcore.difficulty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import dev.cabotmc.hardcore.HardcorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class NightmareDifficulty extends BaseDifficulty {
    static ArrayList<EntityType> blacklist = new ArrayList<>();
    static {
        blacklist.add(EntityType.ENDER_DRAGON);
        blacklist.add(EntityType.ENDER_CRYSTAL);
        blacklist.add(EntityType.SHULKER);
        blacklist.add(EntityType.ZOMBIE);
    }

    public NightmareDifficulty() {
        super(Difficulty.HARD, "Nightmare", 0xff0f0f, Material.SKELETON_SKULL, 4);
    }
    @Override
    public void activate() {
        super.activate();
        Bukkit.getWorld("world").setTime(18000);
        Bukkit.getWorld("world").setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        Bukkit.getServer().getPluginManager().registerEvents(new NightmareListener(), HardcorePlugin.instance);
    

    }
    @Override
    public ArrayList<Component> getInfo() {
        var a = new ArrayList<Component>();
        a.add(Component.text("It is always nighttime", TextColor.color(0xff0f0f)));
        a.add(Component.text("Every mob has a 10% chance of spawning with a 25% buff to movement speed, health, and damage.", TextColor.color(0xff0f0f)));
        a.add(Component.text("Every mob spawn has a 10% chance of getting replaced by an ultra-fast baby zombie.", TextColor.color(0xff0f0f)));
        a.add(Component.text("All rabbits are killer rabbits", TextColor.color(0xff0f0f)));
        return a;
        
    }

    public static class NightmareListener implements Listener {
        long lastBedExplodeTime = 0;
        @EventHandler
        public void spawn(EntitySpawnEvent e) {
            if (!(e.getEntity() instanceof Mob)) return;
            var m = (Mob) e.getEntity();
            var v = m.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (e.getEntityType() == EntityType.RABBIT) {
                Rabbit r = (Rabbit) e.getEntity();
                r.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
                r.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).addModifier(new AttributeModifier("RabbitRange", 16, Operation.ADD_NUMBER));
                return;
            }
            if (v == null || v.getValue() == 0) {
                // peaceful mob
                return;
            }
            if (Math.random() > 0.90) {
                // make miniboss
                v.addModifier(new AttributeModifier("MiniBossDamageMul", 0.25, Operation.MULTIPLY_SCALAR_1));
                m.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(new AttributeModifier("MiniBossHealthMul", 0.25, Operation.MULTIPLY_SCALAR_1));
                m.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("MiniBossHealthMul", 0.25, Operation.MULTIPLY_SCALAR_1));
                HardcorePlugin.MINIBOSS_TEAM.addEntities(m);
                m.setGlowing(true);    
            } else {
                if (Math.random() > 0.90 && !blacklist.contains(e.getEntityType())) {
                    e.setCancelled(true);
                    var l = e.getEntity().getLocation();
                    var w = e.getEntity().getWorld();
                    var spawned = (Zombie) w.spawnEntity(l, EntityType.ZOMBIE);
                    spawned.setBaby();
                    spawned.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).addModifier(new AttributeModifier("RandomZombieRange", 16, Operation.ADD_NUMBER));
                    spawned.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("RandomZombieSpeed", 0.5, Operation.MULTIPLY_SCALAR_1));
                    spawned.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(new AttributeModifier("RandomZombieAttack", -0.5, Operation.MULTIPLY_SCALAR_1));
                    spawned.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(4);
                    m.setMetadata("extra_points", new FixedMetadataValue(HardcorePlugin.instance, 2));
                } 
            }
        }   
        @EventHandler
        public void interact(PlayerInteractEvent e) {
            if (e.getClickedBlock().getType().toString().endsWith("BED") && e.getPlayer().getLocation().getWorld().getName().equals("world")) {
                if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                e.setCancelled(true);
                var w = e.getPlayer().getWorld();
                Fireball f = (Fireball) w.spawnEntity(e.getClickedBlock().getLocation().add(0, 0.5, 0), EntityType.FIREBALL);
                f.customName(Component.text("[intentional game design]"));
                f.setYield(6f);
                f.setIsIncendiary(true);
                f.setDirection(new Vector(0, -1, 0));
                lastBedExplodeTime = Instant.now().toEpochMilli();
            }
        }
        @EventHandler(priority =  EventPriority.LOWEST)
        public void death(PlayerDeathEvent e) {
            // https://cdn.cabotmc.dev/more_info.mp4
            long time = Instant.now().toEpochMilli();
            if (time - lastBedExplodeTime < 500) {
                // died because of bed explode likely
                var base = Component.text(e.getPlayer().getName() + " was killed by ");
                base = base.append(
                    Component.text("[intentional game design]")
                        .clickEvent(ClickEvent.openUrl("https://cdn.cabotmc.dev/more_info.mp4"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click for more info")))
                        .color(TextColor.color(0x0343aeb))
                        .decorate(TextDecoration.UNDERLINED)
                );
                e.deathMessage(base);
            }
        }
    }
}
