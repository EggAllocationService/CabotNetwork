package dev.cabotmc.hardcore.randomdiff.modifiers;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.points.PointsManager;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.player.PlayerUseItemEvent;


public class PeanutGallery extends Modifier implements Listener {
    List<Material> possibleMaterials;
    public PeanutGallery() {
        super("Peanut Gallery");
        possibleMaterials = Arrays.stream(Material.values())
            .filter(m -> m.toString().endsWith("SPAWN_EGG"))
            .toList();

    }
    @Override
    public Component generateDescription() {
        
        return Component.text("Spectators are given a random spawn egg every 30 seconds", TextColor.color(Modifier.BAD_COLOR))
        .append(Component.text("Gain +1 * (# of spectators) to your multiplier", TextColor.color(Modifier.GOOD_COLOR)))
        .append(Component.text("Gain 2 points every time a spectator uses a spawn egg", TextColor.color(Modifier.GOOD_COLOR)));
    }

    @Override
    public void activate() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(HardcorePlugin.instance, () -> this.giveEggs(), 0, 30 * 20);
        PointsManager.addMutator(c -> {
            return c + Bukkit.getOnlinePlayers().stream().filter(d -> d.getGameMode() == GameMode.ADVENTURE).count();
        });
    }

    public void giveEggs() {
        for (var p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL) continue;
            var indexToGive = (int) (Math.floor(Math.random() * possibleMaterials.size()));
            var i = new ItemStack(possibleMaterials.get(indexToGive));
            if (p.getInventory().addItem(i).size() == 0) {
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1f);
            }
        }
    }
    @EventHandler(priority =  EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent e) {
        if (e.getItem().toString().endsWith("SPAWN_EGG")) {
            e.setCancelled(false);
        }
    }
    @EventHandler(priority =  EventPriority.HIGHEST)
    public void use(PlayerUseItemEvent e) {
        if (e.getItemStack().getMaterial().toString().endsWith("SPAWN_EGG")) {
            e.setCancelled(false);
        }
    }
}
