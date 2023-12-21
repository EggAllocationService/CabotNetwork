package dev.cabotmc.mgmt.templates;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileBasedRegistry implements TemplateRegistry {

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

  File baseDir;
  public FileBasedRegistry(File base) {
    baseDir = base;
  }
  @Override
  public Template getByName(String name) {
    var path = baseDir.toPath().resolve(name + ".json");
    if (!path.toFile().exists()) {
      return null;
    }
    String str = null;
    try {
      str = Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new Gson().fromJson(str, Template.class);
  }

  @Override
  public List<Template> getAll() {
    var g = new Gson();
    var files = Arrays.asList(baseDir.listFiles())
            .stream()
            .filter(file -> file.getName().endsWith(".json"))
            .map(File::toPath)
            .toArray(Path[]::new);
    var templates = new ArrayList<Template>();
    for (Path file : files) {
      String string = null;
      try {
        string = Files.readString(file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      var template = g.fromJson(string, Template.class);
      templates.add(template);
    }
    return templates;
  }
}
