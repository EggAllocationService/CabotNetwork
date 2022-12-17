package dev.cabotmc.hardcore.difficulty;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.util.StructureSearchResult;

import dev.cabotmc.hardcore.HardcorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class EasyDifficulty extends BaseDifficulty {
    public EasyDifficulty() {
        super(Difficulty.EASY, "Easy", 0x27e627, Material.LIME_STAINED_GLASS_PANE, 0.75);
    }
    @Override
    public void activate() {
        super.activate();
        var w = Bukkit.getWorld("world");
        var p = Bukkit.getPlayer(HardcorePlugin.ownerName);
        p.setAbsorptionAmount(10);
        StructureSearchResult found = null;
        var arr = new Structure[] {Structure.VILLAGE_PLAINS, Structure.VILLAGE_DESERT, Structure.VILLAGE_SAVANNA, Structure.VILLAGE_SNOWY, Structure.VILLAGE_TAIGA};
        Bukkit.broadcast(Component.text("Searching for a village...", TextColor.color(color)));
        for (var s : arr) {
            found = w.locateNearestStructure(p.getLocation(), s, 4000, false);
            if (found != null) break;
        }
        if (found == null) {
            Bukkit.broadcast(Component.text("Couldn't find a village! Weird seed?"));
            return;
        }
        final var foundLoc = found.getLocation();
        Bukkit.broadcast(Component.text("Found a village! Waiting for chunk load before teleporting you...", TextColor.color(color)));
        w.getChunkAt(found.getLocation());
        w.getWorldBorder().setSize(640000);
        var tploc = w.getHighestBlockAt(foundLoc).getLocation().add(0, 1, 0);
        for (var d : Bukkit.getOnlinePlayers()) {
            d.teleport(tploc);
        }

    }
    @Override
    public ArrayList<Component> getInfo() {
        var a = new ArrayList<Component>();
        a.add(Component.text("You are guarenteed to spawn in a village", TextColor.color(color)));
        a.add(Component.text("You have an extra five hearts of overhealth when you spawn", TextColor.color(color)));
        return a;
        
    }
}
