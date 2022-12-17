package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class FreeSteak extends Modifier {
    public FreeSteak() {
        super("Free Steak!");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Gain 16 steak off spawn", TextColor.color(Modifier.GOOD_COLOR));
    }

    @Override
    public void activate() {
       var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
       p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
        
    }
    
}