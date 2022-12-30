package dev.cabotmc.hardcore.randomdiff.modifiers;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.randomdiff.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.w3c.dom.Attr;

public class FreeDog extends Modifier {
    public FreeDog() {
        super("Man's Best Friend");
    }

    public Component generateDescription() {
        return Component.text("You spawn with a strong dog friend.", TextColor.color(Modifier.GOOD_COLOR));
    }

    public void activate() {
        var world = Bukkit.getWorld("world");
        Player player = Bukkit.getPlayer(HardcorePlugin.ownerName);
        var wolf = (Wolf)world.spawnEntity(player.getLocation(), EntityType.WOLF) ;
        wolf.setOwner(player);
        wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10);
    }
}