package dev.cabotmc.mgmt.containers;

import com.esotericsoftware.kryonet.Connection;
import dev.cabotmc.mgmt.templates.Template;

public class WrappedContainer {
    public String containerID;
    public Template template;

    public Connection containerConnection;
}
