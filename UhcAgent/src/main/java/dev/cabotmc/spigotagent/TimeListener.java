package dev.cabotmc.spigotagent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;

import dev.cabotmc.commonnet.CommonClient;
import dev.cabotmc.spigotagent.tickets.TicketUtil;

public class TimeListener implements Listener {
    @EventHandler
    public void change(UhcGameStateChangedEvent e) {
        if (e.getOldGameState() == GameState.LOADING && e.getNewGameState() == GameState.WAITING) {
            CommonClient.notifyQueue();
        } else if (e.getNewGameState() == GameState.ENDED) {
            CommonClient.sendMessageToServer("velocity", "queue:uhc:done");
        } else if (e.getNewGameState() == GameState.PLAYING) {
            GameManager.getGameManager().getTeamManager().getUhcTeams().forEach(u -> {
                var l = u.getLeader();
                if (l.isOnline()) {
                    try {
                        var p = l.getPlayer();
                        TicketUtil.giveBlankTicketToPlayer(p);
                    } catch (UhcPlayerNotOnlineException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
    }
    @EventHandler
    public void leave(PlayerQuitEvent e) {
        if (Bukkit.getOnlinePlayers().size() == 1) {
            // last player quit
            Bukkit.shutdown();
        } 
    }
    @EventHandler
    public void map(PlayerAttemptPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.FILLED_MAP || e.getItem().getItemStack().getType() == Material.MAP) {
            e.setCancelled(true);
        }
    }
}
