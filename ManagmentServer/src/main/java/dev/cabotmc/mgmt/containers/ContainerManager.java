package dev.cabotmc.mgmt.containers;

import com.github.dockerjava.api.model.Container;
import dev.cabotmc.mgmt.Main;
import dev.cabotmc.mgmt.templates.TemplateRegistry;

import java.util.HashMap;

public class ContainerManager {
    public static HashMap<String, WrappedContainer> trackedContainers = new HashMap<>();
    public static void loadRunningContainers() {
        var results = Main.docker.listContainersCmd().exec();
        for (var result : results) {
            if (!result.labels.containsKey("cabot-template") || !result.labels.containsKey("cabot-name")) {
                continue;
            }
            var templateLabel = result.labels.get("cabot-template");
            if (!TemplateRegistry.templates.containsKey(templateLabel)) {
                System.out.println("Container " + result.getId() + " has an invalid template label: " + templateLabel);
                continue;
            }
            var container = new WrappedContainer();
            container.container = result;
            container.template = TemplateRegistry.templates.get(templateLabel);
            trackedContainers.put(result.labels.get("cabot-name"), container);
        }
    }
}
