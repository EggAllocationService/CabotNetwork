package dev.cabotmc.velocityagent;

import com.velocitypowered.api.command.SimpleCommand;

import dev.cabotmc.mgmt.protocol.CreateServerRequestMessage;
import net.kyori.adventure.text.Component;

public class CreateCommand implements SimpleCommand{

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        var template = args[0];
        var msg = new CreateServerRequestMessage();
        msg.templateName = template;
        VelocityAgent.kryoClient.sendTCP(msg);
        invocation.source().sendMessage(Component.text("Sent request to create " + template));
    }
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return true;
    }
    
}
