package dev.cabotmc.skins;

import org.bson.codecs.pojo.annotations.BsonId;

public class SkinReplacementRecord {
    @BsonId
    public String originalValue;
    public String replacementValue;
    public String replacementSignature;
}
