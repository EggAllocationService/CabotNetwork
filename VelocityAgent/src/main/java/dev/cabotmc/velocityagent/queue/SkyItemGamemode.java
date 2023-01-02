package dev.cabotmc.velocityagent.queue;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.packet.BossBar;

import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntArrayTag;
import net.querz.nbt.tag.ListTag;

public class SkyItemGamemode extends AbstractSurvivalGamemode {
    public static final String PRESENT_TAG = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzUzYzg5YTJhZGM0ZWU1YmExZjA1ZTVkNjRlOWI0YmI2YjMyMzJjNzIwMjhlMGNiZTM1ZTFiNzNkMGM1N2RjMSJ9fX0=";
    public static final int[] OWNER_ID = {-1214522970,-176534983,-1226267444,537468470};   
    CompoundTag SkullOwnerTag;
    public SkyItemGamemode() {
        super("skysurvival", 0x87e8e5, Color.BLUE);
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
        var i = new ItemStack(ItemType.PLAYER_HEAD);
        i.nbtData().put("SkullOwner", SkullOwnerTag);
        var base = Component.text("Sky Survival ", TextColor.color(COLOR)).decoration(TextDecoration.ITALIC, false);
        base = base.append(Component.text("\uE00C", TextColor.color(0xfaea5a)).style(builder -> {
            builder.decoration(TextDecoration.ITALIC, false);
            builder.font(Key.key("cabot", "icons"));
            
        }));
        i.displayName(base);
        return i;
    }
    @Override
    public boolean hasPermission(Player p) {
        return p.hasPermission("gamemode.skysurvival");
    }
    
}
