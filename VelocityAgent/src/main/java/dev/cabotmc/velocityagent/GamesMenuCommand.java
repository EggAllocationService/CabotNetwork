package dev.cabotmc.velocityagent;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.velocityagent.menus.GamesMenu;

public class GamesMenuCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) return;
        var p = (Player) invocation.source();
        new GamesMenu().open(p);
    }
    
}
