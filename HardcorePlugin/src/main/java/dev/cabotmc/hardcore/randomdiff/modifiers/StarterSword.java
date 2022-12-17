package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class StarterSword extends Modifier {
    public StarterSword() {
        super("Free Sword!");
    }
    @Override
    public Component generateDescription() {
        return Component.text("Gain an iron sword off spawn", TextColor.color(Modifier.GOOD_COLOR));
    }

    @Override
    public void activate() {
       var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
       p.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        
    }
    
}
