package dev.cabotmc.velocityagent.queue;

import com.velocitypowered.api.proxy.Player;

import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntArrayTag;
import net.querz.nbt.tag.ListTag;

public class DepartmasGamemode extends AbstractSurvivalGamemode {
    public static final String PRESENT_TAG = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTQ3YjM3ZTY3YTg5MTU5YmY0YWNjNGE0NGQ0MzI4ZjRlZmMwMTgxNjA1MTQyMjg4ZTVlZWQxYWI4YWVkOTEzYyJ9fX0=";
    public static final int[] OWNER_ID = {-168448369,230706505,-1264922710,-797731259};    
    CompoundTag SkullOwnerTag;
    public DepartmasGamemode() {
        super("departmas", 0xfa0a56, BossBar.Color.RED);
        var valueTag = new CompoundTag();
        valueTag.putString("Value", PRESENT_TAG);
        var texturesTag = new ListTag<>(CompoundTag.class);
        texturesTag.add(valueTag);
        var propertiesTag = new CompoundTag();
        propertiesTag.put("textures", texturesTag);
        SkullOwnerTag = new CompoundTag();
        SkullOwnerTag.put("Properties", propertiesTag);
        var idTag = new IntArrayTag(OWNER_ID);
        SkullOwnerTag.put("Id", idTag);
    }

    @Override
    public ItemStack createIcon() {
        var icon = new ItemStack(ItemType.PLAYER_HEAD);
        icon.nbtData().put("SkullOwner", SkullOwnerTag);
        icon.displayName(Component.text("Departmas Survival", TextColor.color(COLOR)).decoration(TextDecoration.ITALIC, false));
        return icon;
    }
    @Override
    public boolean hasPermission(Player p) {
        return p.hasPermission("gamemode.departmas");
    }
    
}
