package dev.cabotmc.velocityagent.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import dev.cabotmc.mgmt.protocol.CreateServerRequestMessage;
import dev.cabotmc.velocityagent.VelocityAgent;
import dev.simplix.protocolize.api.item.ItemStack;

public abstract class Queue {
    String name;
    ArrayList<String> tokens = new ArrayList<>();
    public Queue(String name) {
        this.name = name;
    }
    public abstract void addPlayer(Player p);
    public abstract void removePlayer(Player p);
    public abstract void onServerCreate(String serverName, String token);
    public abstract boolean isInQueue(Player p);
    public String requestCreateServer(String templateName) {
        return requestCreateServer(templateName, new String[0]);
    }
    
    public String requestCreateServer(String templateName, String[] envArgs) {
        var p = new CreateServerRequestMessage();
        
        var tmp2 = new String[envArgs.length + 1];
        var token = UUID.randomUUID().toString().split("-")[0];
        int index = 0;
        for (String s : envArgs) {
            tmp2[index] = s;
            index++;
        }
        tmp2[index] = "QUEUE_TOKEN=" + token;
        p.enviromentVars = tmp2;
        p.templateName = templateName;
        VelocityAgent.kryoClient.sendTCP(p);
        tokens.add(token);
        return token;
    }
    public boolean waitingForToken(String s) {
        if (tokens.contains(s)) {
            tokens.remove(s);
            return true;
        } else {
            return false;
        }
    }
    
    public String getName() {
        return name;
    }   

    public abstract ItemStack createIcon();
    public boolean hasPermission(Player p) {
        return true;
    }
    public ItemStack createServerIcon(RegisteredServer s) {
        return null;
    }
}
