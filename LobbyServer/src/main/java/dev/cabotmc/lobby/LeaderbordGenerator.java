package dev.cabotmc.lobby;

import java.util.ArrayList;
import java.util.Collections;

import dev.cabotmc.chc.CompHardcorePlayer;
import dev.cabotmc.lobby.db.Database;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.Player;

public class LeaderbordGenerator {
    public static void showLeaderboard(Player p) {
        p.sendMessage(Component.text("Leaderboard for competitve hardcore:"));
        var things = new ArrayList<CompHardcorePlayer>();
        for (CompHardcorePlayer i : Database.comphc.find()) {
            i.bestScore = round(i.bestScore);
            things.add(i);
        }
        Collections.sort(things);
        int place = 1;
        for (var t : things) {
            p.sendMessage(createPlaceMessage(t, place));
            place++;
        }
    }
    public static Component createPlaceMessage(CompHardcorePlayer p, int i) {
        var start = Component.text(i + ". ", TextColor.color(0xd2f39e));
        start = start.append(Component.text(p.displayName + ": ", TextColor.color(0xd48633)));
        start = start.append(Component.text("" + p.bestScore, TextColor.color(0x33d486)));
        return start;
    }  
    public static double round(double i) {
        var x = i * 100;
        x = Math.round(x);
        return x / 100; 
    } 
}
