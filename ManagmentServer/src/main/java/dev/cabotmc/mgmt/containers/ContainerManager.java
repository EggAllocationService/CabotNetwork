package dev.cabotmc.mgmt.containers;

import com.github.dockerjava.api.model.Container;
import dev.cabotmc.mgmt.Main;
import dev.cabotmc.mgmt.templates.Template;
import dev.cabotmc.mgmt.templates.TemplateRegistry;

import java.util.HashMap;
import java.util.UUID;

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
            container.containerID = result.getId();
            container.template = TemplateRegistry.templates.get(templateLabel);
            trackedContainers.put(result.labels.get("cabot-name"), container);
        }
    }
    public static void requestContainerStart(Template template) {
        var container = new WrappedContainer();
        var name = template.name + "-" + UUID.randomUUID().toString().split("-")[0];
        var backend = Main.docker.createContainerCmd(template.dockerImage).withName(name)
                .withLabels(new HashMap<String, String>() {{
                    put("cabot-template", template.name);
                    put("cabot-name", name);
                }})
                .withEnv("CABOT_NAME=" + name);
        if (template.forwardPort != 0) {
            backend = backend.withPortSpecs(template.forwardPort + ":" + template.forwardPort);
        }
        var created = backend.exec();
        container.containerID = created.getId();
        container.template = template;
        trackedContainers.put(name, container);
    }

}