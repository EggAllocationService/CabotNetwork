package dev.cabotmc.velocityagent;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import dev.cabotmc.velocityagent.menus.ServersMenu;

public class ServersCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) return;
        var p = (Player) invocation.source();
        new ServersMenu().open(p);
    }
    
}
