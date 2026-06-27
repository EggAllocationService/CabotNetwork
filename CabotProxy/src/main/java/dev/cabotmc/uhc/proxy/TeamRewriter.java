package dev.cabotmc.uhc.proxy;

import com.velocitypowered.api.TextHolder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.scoreboard.TeamEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scoreboard.ScoreboardManager;
import com.velocitypowered.api.scoreboard.TeamColor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TeamRewriter {
    LuckPerms lp;
    ScoreboardManager manager;

    ProxyServer proxy;

    HashSet<Player> toUpdate = new HashSet<>();

    public TeamRewriter(ProxyServer proxy) {
        this.proxy = proxy;
        this.lp = LuckPermsProvider.get();
        this.manager = ScoreboardManager.getInstance();
    }

    @Subscribe
    public void serverSwitch(ServerPostConnectEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    @Subscribe
    public void onTeamUpdate(TeamEvent.AddPlayers event) {
        // schedule recompute for that player in the future
        final var player = event.getPlayer();
        scheduleUpdate(player);
    }

    @Subscribe
    public void teamPropsUpdate(TeamEvent.Update event) {
        scheduleUpdate(event.getPlayer());
    }

    @Subscribe
    public void onTeamRemove(TeamEvent.RemovePlayers event) {
        // schedule recompute for that player in the future
        final var player = event.getPlayer();
        scheduleUpdate(player);
    }

    private void scheduleUpdate(Player player) {
        if (toUpdate.contains(player)) return;

        toUpdate.add(player);

        proxy.getScheduler().buildTask(
                        CabotProxy.instance,
                        () -> {
                            toUpdate.remove(player);
                            recomputeTeamsForPlayer(player);
                        }
                )
                .delay(300, TimeUnit.MILLISECONDS)
                .schedule();
    }

    public void recomputeTeamsForPlayer(Player p) {
        if (p.getCurrentServer().isEmpty()) return;
        var b = manager.getBackendScoreboard(p);
        var a = manager.getProxyScoreboard(p);
        var players = p.getCurrentServer().get().getServer().getPlayersConnected();
        Map<Player, String> backendPlayersByTeam = new HashMap<>();
        Map<Player, String> proxyPlayersByTeam = new HashMap<>();

        // build map of team names for player
        for (var player : players) {
            var frontendTeam = b.getTeams().stream().filter(
                    team -> team.getEntries().contains(player.getUsername())
            ).findFirst();

            frontendTeam.ifPresent(team -> backendPlayersByTeam.put(player, team.getName()));

            var backendTeam = a.getTeams().stream().filter(
                    team -> team.getEntries().contains(player.getUsername())
            ).findFirst();
            backendTeam.ifPresent(team -> proxyPlayersByTeam.put(player, team.getName()));
        }

        // compute each players team
        for (var player : players) {
            var meta = lp.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData();
            if (!backendPlayersByTeam.containsKey(player) && meta.getPrefix() == null && meta.getSuffix() == null) {
                // player is not in a team, and needs no name formatting
                // so just remove them from a team if they're in one
                if (proxyPlayersByTeam.containsKey(player)) {
                    a.getTeam(proxyPlayersByTeam.get(player)).removeEntry(player.getUsername());
                }
            } else {
                var team = a.getTeam(player.getUsername());
                if (team == null) {
                    team = a.createTeam(player.getUsername(), l -> {});
                }
                if (!team.getEntries().contains(player.getUsername())) {
                    team.addEntry(player.getUsername());
                }

                team.updateProperties(x -> {
                    // set prefix and suffix correctly
                    if (meta.getPrefix() != null) {
                        x.prefix(
                                TextHolder.of(
                                        MiniMessage.miniMessage().deserialize(
                                                meta.getPrefix() + "<reset> "
                                        )
                                )
                        );
                    }

                    Component suffix = Component.empty();
                    if (backendPlayersByTeam.containsKey(player)) {
                        var backendTeam = b.getTeam(backendPlayersByTeam.get(player)).getSuffix();
                        suffix = suffix.append(backendTeam.getModernText());
                    }

                    if (meta.getSuffix() != null) {
                        suffix = suffix.append(MiniMessage.miniMessage().deserialize(
                                "<reset> " + meta.getSuffix()
                        ));
                    }

                    x.suffix(
                            TextHolder.of(
                                suffix
                            )
                    );

                    // if `p` is in a team then we need to set the team color based on whether `player` is on the same backend team as `p` or not
                    if (backendPlayersByTeam.containsKey(player) && backendPlayersByTeam.containsKey(p) && player != p) {
                        x.color(
                                backendPlayersByTeam.get(player).equals(backendPlayersByTeam.get(p)) ? TeamColor.GREEN : TeamColor.RED
                        );
                    } else {
                        x.color(TeamColor.RESET);
                    }
                });
            }
        }
    }
}
