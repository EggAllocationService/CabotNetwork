package dev.cabotmc.mgmt.containers;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.VolumeBind;
import com.github.dockerjava.api.model.VolumeBinds;

import dev.cabotmc.mgmt.Main;
import dev.cabotmc.mgmt.templates.Template;
import dev.cabotmc.mgmt.templates.TemplateRegistry;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static void requestContainerStart(Template template, String[] env) {

        var name = template.name + "-" + UUID.randomUUID().toString().split("-")[0];
        startContainerWithName(template, name, env);
    }


    public static void startContainerWithName(Template template, String name, String[] envargs) {
        var container = new WrappedContainer();
        if (template.persistent) {
            // persistant templates can only have one so set the name
            name = template.name;
        }
        if (trackedContainers.containsKey(name)) {
            System.out.println("ERROR: Creating container that already exists!");
            return;
        }
        var backend = Main.docker.createContainerCmd(template.dockerImage).withName(name);
                /* .withLabels(new HashMap<String, String>() {
                    {
                        put("cabot-template", template.name);
                        put("cabot-name", name);
                    }
                });*/
                
        if (envargs == null || envargs.length == 0) {
            backend = backend.withEnv("CABOT_NAME=" + name);
        } else {
            var list = new ArrayList<String>(Arrays.asList(envargs));
            list.add("CABOT_NAME=" + name);
            backend = backend.withEnv(list);
        }
        
        var hostConfig = backend.getHostConfig()
                .withAutoRemove(true);
        backend = backend.withHostConfig(hostConfig);
        if (template.forwardPort != 0) {
            backend = backend.withPortBindings(PortBinding.parse(template.forwardPort + ":" + template.forwardPort));
        }
        if (template.persistent) {
            if (template.mountPath != null) {
                backend.getHostConfig().setBinds(new Bind(template.mountPath, new Volume("/data")));
            } else {
                // create a volume for that template and mount it under data implement later
            }
        }
        var created = backend.exec();
        Main.docker.startContainerCmd(created.getId()).exec();
        container.containerID = created.getId();
        container.template = template;
        trackedContainers.put(name, container);
    }
}
