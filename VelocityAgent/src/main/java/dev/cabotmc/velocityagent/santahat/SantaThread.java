package dev.cabotmc.velocityagent.santahat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.mineskin.MineskinClient;
import org.mineskin.data.Skin;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;

import dev.cabotmc.skins.SkinReplacementRecord;
import dev.cabotmc.velocityagent.VelocityAgent;
import dev.cabotmc.velocityagent.db.Database;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SantaThread extends Thread {
    static ArrayList<SantaJob> jobs = new ArrayList<>();
    static MineskinClient client;
    public SantaThread() {
        super("Santa Creator");
        client = new MineskinClient("Santifier", "89988f9df45a379fc26eee005df5ef3b70b8d758da35bd45b6f7dca327df1482");
    }
    @Override
    public void run() {
        while(true) {
            while(true) {
                if (jobs.size() != 0) break;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            var job = jobs.get(0);
            var jobId = UUID.randomUUID().toString().split("-")[0];
            URL plainSkinUrl;
            try {
                plainSkinUrl = new URL("https://crafatar.com/skins/" + job.playerId);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }
            var plainSkinFile = "/tmp/" + jobId + ".png";
            try {
                Files.copy(plainSkinUrl.openStream(), Path.of(plainSkinFile), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            var cmd = makeCommand(jobId);
            try {
                Runtime.getRuntime().exec(cmd).waitFor();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            var f = new File("/tmp/" + jobId + "-out.png");
            Skin result;
            try {
                result = client.generateUpload(f).join();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        
            try {
                Files.deleteIfExists(Path.of(plainSkinFile));
                Files.deleteIfExists(f.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            var record = new SkinReplacementRecord();
            record.originalValue = job.originalTexture;
            record.replacementSignature = result.data.texture.signature;
            record.replacementValue = result.data.texture.value;
            Database.skins.insertOne(record);
            jobs.remove(0);
            var replacementProps = new ArrayList<GameProfile.Property>();
            replacementProps.add(new GameProfile.Property("textures", record.replacementValue, record.replacementSignature));
            var p = VelocityAgent.getProxy().getPlayer(UUID.fromString(job.playerId));
            if (p.isPresent()) {
                var pp = p.get();
                VelocityAgent.getProxy().getScheduler().buildTask(VelocityAgent.instance, () -> {
                    pp.setGameProfileProperties(replacementProps);
                    pp.sendMessage(Component.text("Your santa hat has been applied. You may need to relog to see it.", TextColor.color(0xca4040)));
                    pp.createConnectionRequest(VelocityAgent.getProxy().getServer("lobby").get()).fireAndForget();
                }).delay(2, TimeUnit.SECONDS).schedule();
            }

        }
    }

    static String makeCommand(String jobID) {
        return "/usr/bin/convert /tmp/" + jobID + ".png /velocity/santa.png -composite /tmp/" + jobID + "-out.png" ;
    }
    public static void addJob(SantaJob j) {
        for (var d : jobs) {
            if (d.playerId.equals(j)) return; // prevent duplicates
        }
        jobs.add(j);
    }
}
