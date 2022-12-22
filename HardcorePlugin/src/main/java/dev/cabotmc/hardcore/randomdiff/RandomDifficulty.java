package dev.cabotmc.hardcore.randomdiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import dev.cabotmc.hardcore.HardcorePlugin;
import dev.cabotmc.hardcore.difficulty.BaseDifficulty;
import dev.cabotmc.hardcore.randomdiff.modifiers.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RandomDifficulty extends BaseDifficulty {
    boolean finalized = false;
    public ArrayList<Modifier> finalModifiers = null;
    ArrayList<Modifier> availableModifiers = new ArrayList<>();
    public RandomDifficulty() {
        super(randDifficulty(), "????????", randColor(), randMaterial(), (Math.floor(Math.random() * 200.0) / 100) + 0.75);
        availableModifiers.add(new BlindFall());
        availableModifiers.add(new FastCreepers());
        availableModifiers.add(new F3Menu());
        availableModifiers.add(new InstaDeath());
        availableModifiers.add(new DoublePoints());
        availableModifiers.add(new StarterSword());
        availableModifiers.add(new RandomTP());
        availableModifiers.add(new MissHits());
        availableModifiers.add(new XPHealth());
        availableModifiers.add(new FreeSteak());
        availableModifiers.add(new RabbitMega());
        availableModifiers.add(new Popularity());
        availableModifiers.add(new DJChan());
        availableModifiers.add(new BonkStick());
        availableModifiers.add(new RandomDrops());
        //availableModifiers.add(new WtfPts());
    }
    static int randColor() {    
        return (int) Math.floor(Math.random() * 0xFFFFFF);
    }
    static Difficulty randDifficulty() {
        var x = Difficulty.values();
        var index = Math.floor(Math.random() * x.length);
        return x[(int) index];
    }
    static Material randMaterial() {
        var ms = Material.values();
        var index = Math.floor(Math.random() * ms.length);
        var m = ms[(int) index];
        if (!m.isItem()) {
            return randMaterial();
        }
        return m;
    }
    @Override
    public void activate() {
        if (setting == Difficulty.PEACEFUL) {
            setting = Difficulty.EASY;
        }
        super.activate();
        for (Modifier m : finalModifiers) {
            m.activate();
            if (m instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) m, HardcorePlugin.instance);
            }
        }
        for (Modifier m : finalModifiers) {
            m.postActivate();
        }
    }
    @Override
    public ArrayList<Component> getInfo() {
        if (finalized) {
            var arr = new ArrayList<Component>();
            arr.add(Component.text("Hover above the modifiers to learn more:", TextColor.color(0x1e9dc7)).decorate(TextDecoration.BOLD));
            finalModifiers.stream()
                .map(c -> c.generateInfo())
                .forEach(c -> arr.add(c));
            return arr;
        }

        var m = new ArrayList<Component>();
        int size = (int) Math.round(Math.random() * 3) + 3;
        Collections.shuffle(availableModifiers);
        for (int i = 0; i < size && i < availableModifiers.size(); i++) {
            m.add(availableModifiers.get(i).generateInfo().decorate(TextDecoration.OBFUSCATED));
        }
        return m;
    }

    @Override
    public void onRender() {
        color = randColor();
        displayMat = randMaterial();
        setMultiplier((Math.floor(Math.random() * 200.0) / 100) + 0.75);
    }
    @Override
    public void finalize() {
        name = "Randomized";
        int size = (int) Math.round(Math.random() * 3) + 3;
        var x = new ArrayList<Modifier>();
        Collections.shuffle(availableModifiers);
        for (int i = 0; i < size && i < availableModifiers.size(); i++) {
            x.add(availableModifiers.get(i));
        }
        x.add(new PrintDiffModifier());
        finalModifiers = x;
        setMultiplier((Math.floor(Math.random() * 200.0) / 100) + 0.75);
        finalized = true;
    }
}
