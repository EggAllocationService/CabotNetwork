package dev.cabotmc.hardcore.difficulty;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BaseDifficulty {
    public Difficulty setting;
    public String name;
    public int color;
    public Material displayMat;
    double multiplier;
    public BaseDifficulty(Difficulty d, String name, int color, Material displayMaterial, double multiplier) {
        setting = d;
        this.name = name;
        this.color = color;
        this.multiplier = multiplier;
        this.displayMat = displayMaterial;
    }
    public Component toText() {
        return Component.text(name, TextColor.color(color));
    }
    public double getMultiplier() {
        return multiplier;
    }
    public void setMultiplier(double d) {
        multiplier = d;
    }
    public ArrayList<Component> getInfo() {
        return new ArrayList<>();
    }
    public void finalize() {

    }
    public void onRender() {

    }
    public void activate() {
        for (World w : Bukkit.getWorlds()) {
            w.setDifficulty(setting);
        }
    }
}
