package dev.cabotmc.spigotagent.elo;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.UhcPlayer;

import dev.cabotmc.spigotagent.SpigotAgent;
import forwardloop.glicko2s.EloResult;
import forwardloop.glicko2s.Glicko2;
import forwardloop.glicko2s.Glicko2J;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import scala.Tuple2;

public class EloListener implements Listener {
    @EventHandler
    public void join(PlayerJoinEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(SpigotAgent.instance, () -> {
            var msg = Component.text("Your current ELO: ", TextColor.color(0xbf4ba8));
            msg = msg.append(Component.text(formatGlickoElo(EloService.getRating(e.getPlayer().getUniqueId()).rating), TextColor.color(0x5bde3e)));
            e.getPlayer().sendMessage(msg);
        }, 80L);    
    }
    

    @EventHandler
    public void death(PlayerDeathEvent e) {
        var loser = e.getPlayer();
        var loserTeam = GameManager.getGameManager().getPlayerManager().getOrCreateUhcPlayer(loser).getTeam();
        for (var p : GameManager.getGameManager().getPlayerManager().getAllPlayingPlayers()) {
            if (!p.getTeam().equals(loserTeam)) {
                EloService.recordResult(p.getUuid(), loser.getUniqueId());
            }
        }
    }
    @EventHandler
    public void onFinish(UhcGameStateChangedEvent e) {
        if (e.getNewGameState() == GameState.ENDED) {
            //
            try {
                var m = GameManager.getGameManager().getPlayerManager().getClass().getDeclaredMethod("getWinners");
                m.setAccessible(true);
                List<UhcPlayer> results = (List<UhcPlayer>) m.invoke(GameManager.getGameManager().getPlayerManager());
                var winningTeam = results.get(0).getTeam();
                for (var p : GameManager.getGameManager().getPlayerManager().getAllPlayingPlayers()) {
                    if (!results.contains(p) && !p.getTeam().equals(winningTeam)) {
                        for (var winner: results) {
                            EloService.recordResult(winner.getUuid(), p.getUuid());
                        }
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            
        }
        for (var u : EloService.results.keySet()) {
            // u is the user we're reevaluating the rating for
            var currentRating = EloService.getRating(u);
            var storedNum = currentRating.rating;
            var baseGlicko = currentRating.toGlicko();
            var results = EloService.getResults(u);
            List<Tuple2<Glicko2, EloResult>> formattedResults = results.keySet().stream()
                .map(ru -> new Tuple2<Glicko2, EloResult>(EloService.getRating(ru).toGlicko(), results.get(ru)))
                .toList();
            var newRating = Glicko2J.calculateNewRating(baseGlicko, formattedResults);
            currentRating.copyRatingsFromGlicko(newRating);
            EloService.updateRating(currentRating.id);
            if (Bukkit.getPlayer(u) != null) {
                Bukkit.getPlayer(u).sendMessage(createEloChangeMessage(storedNum, newRating.rating()));
            }
            System.out.println(u.toString() + " " + storedNum + " -> " + newRating.rating());
        }
    }
    
    static Component createEloChangeMessage(double elo1, double elo2) {
        var base = Component.text("Your ELO has been recalculated: ", TextColor.color(0xbf4ba8));
        if (elo1 < elo2) {
            // gained elo
            base = base.append(Component.text(formatGlickoElo(elo1), TextColor.color(0xeb4034)));
            base = base.append(Component.text(" \u2192 ", TextColor.color(0xbf4ba8)));
            base = base.append(Component.text(formatGlickoElo(elo2), TextColor.color(0x5bde3e)));
        } else {
            base = base.append(Component.text(formatGlickoElo(elo1), TextColor.color(0x5bde3e)));
            base = base.append(Component.text(" \u2192 ", TextColor.color(0xbf4ba8)));
            base = base.append(Component.text(formatGlickoElo(elo2), TextColor.color(0xeb4034)));
        }
        return base;
    }   
    static String formatGlickoElo(double elo) {
        var d = (int) Math.round(400 + (elo * 20));
        return Integer.toString(d);
    }
} 
