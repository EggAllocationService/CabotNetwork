package dev.cabotmc.random;

import io.papermc.paperclip.Paperclip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        deleteDir(new File("world/datapacks/randomizer"));
        Files.deleteIfExists(Path.of("computed.json"));

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
        var coolPattern = Pattern.compile("minecraft:[a-z_]*");
        for (int i =0; i < inputs.size(); i++) {
            var o = createCopyPaths(inputs.get(i), outputs.get(i));
            
            
            Files.copy(o[0], o[1]);
            var targetFile = o[1].toFile();
            var s = new Scanner(targetFile);
            String x;
            while (s.hasNextLine()) {
                x = s.nextLine();
                if (!x.contains("\"name\"")) continue;
                var droppedItem = coolPattern.matcher(x);
                
                if (droppedItem.find()) {
                    var itemString = droppedItem.group(0);
                    var provider = targetFile.getName().replace(".json", "");
                    CachedRelationships.storeMatch(provider, itemString);
                }
                
            }

        }
        CachedRelationships.saveFile();
        System.out.println("Randomized loot tables");
        Paperclip.main(new String[] {"nogui"});
    }

    public static Path[] createCopyPaths(String inputPath, String outputPath) throws IOException {
        var o = new Path[2];
        o[0] = Path.of(inputPath);
        o[1] = Path.of("world/datapacks/randomizer/" + outputPath);
        Files.createDirectories(o[1].getParent());
        return o;
    }
    static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }
}
