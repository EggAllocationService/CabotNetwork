package dev.cabotmc.mgmt.templates;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public interface TemplateRegistry {
    Template getByName(String name);
    List<Template> getAll();

    /*public static HashMap<String, Template> templates = new HashMap<>();

    public static void initFromFolder(File folder) throws IOException {
        var g = new Gson();
        var files = Arrays.asList(folder.listFiles())
                .stream()
                .filter(file -> file.getName().endsWith(".json"))
                .map(File::toPath)
                .toArray(Path[]::new);
        for (Path file : files) {
            var string = Files.readString(file);
            var template = g.fromJson(string, Template.class);
            templates.put(template.name, template);
        }
    }*/

}
