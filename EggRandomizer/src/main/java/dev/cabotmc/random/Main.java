package dev.cabotmc.random;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        var paths = Files.walk(Path.of("data"));
        var inputs = paths.filter(Files::isRegularFile)
            .map(Path::toString)
            .collect(Collectors.toList());
        var outputs = new ArrayList<String>(inputs.size());
        outputs.addAll(inputs);
        Collections.shuffle(outputs);
        
        new File("world/datapacks/randomizer").mkdirs();

        var packFile = Main.class.getClassLoader().getResourceAsStream("pack.mcmeta");
        var mcmeta = new FileOutputStream("world/datapacks/randomizer/pack.mcmeta");
        packFile.transferTo(mcmeta);
        mcmeta.close();
        packFile.close();

        for (int i =0; i < inputs.size(); i++) {
            var o = createCopyPaths(inputs.get(i), outputs.get(i));
            Files.copy(o[0], o[1]);
        }
        System.out.println("Randomized loot tables");
        if (args.length != 0) {
            System.out.println("Loading and executing main class...");
            var targetClass = Class.forName(args[0]);
            var mainMethod = targetClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[]{});
        }


    }

    public static Path[] createCopyPaths(String inputPath, String outputPath) throws IOException {
        var o = new Path[2];
        o[0] = Path.of(inputPath);
        o[1] = Path.of("world/datapacks/randomizer/" + outputPath);
        Files.createDirectories(o[1].getParent());
        return o;
    }
}
