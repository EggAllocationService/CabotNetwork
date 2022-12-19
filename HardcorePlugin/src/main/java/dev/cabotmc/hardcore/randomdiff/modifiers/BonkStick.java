package dev.cabotmc.hardcore.randomdiff.modifiers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BonkStick extends Modifier {
    public BonkStick() {
        super("Horny Police Officer");
    }

    @Override
    public Component generateDescription() {
        return Component.text("Gain a free bonk stick", TextColor.color(Modifier.GOOD_COLOR));
    }

    @Override
    public void activate() {
        var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
        var i = new ItemStack(Material.STICK);
        var m = i.getItemMeta();
        m.addEnchant(Enchantment.KNOCKBACK, 8, true);
        m.displayName(Component.text("Horny Police Baton", TextColor.color(0x4ac6a1)).decoration(TextDecoration.ITALIC,
                false));
        i.setItemMeta(m);
        p.getInventory().addItem(i);
    }

}
