package dev.cabotmc.mgmt.templates;

public class Template {
    public String name;
    public String dockerImage;
    public boolean persistent;
    public String mountPath;
    public int forwardPort;
}
