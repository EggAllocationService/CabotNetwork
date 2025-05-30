package dev.cabotmc.spigotagent;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import dev.cabotmc.spigotagent.tickets.TicketBrowseMenu;

public class DebugTicketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("uhc.debugticket")) return false;
        var p = (Player) sender;
        new TicketBrowseMenu(p, p.getGameMode() == GameMode.CREATIVE, args.length != 0).open();
        return true;
    }
    
}
