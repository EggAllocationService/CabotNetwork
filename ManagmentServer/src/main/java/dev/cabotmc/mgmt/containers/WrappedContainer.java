package dev.cabotmc.mgmt.containers;

import com.github.dockerjava.api.model.Container;
import dev.cabotmc.mgmt.templates.Template;

public class WrappedContainer {
    public Container container;
    public Template template;
}
