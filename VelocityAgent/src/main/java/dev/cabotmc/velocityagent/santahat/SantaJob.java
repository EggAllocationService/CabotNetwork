package dev.cabotmc.velocityagent.santahat;

public class SantaJob {
    public String playerId;
    public String originalTexture;
    public SantaJob(String playerUUID, String origTex) {
        playerId = playerUUID;
        originalTexture = origTex;
    }
}
