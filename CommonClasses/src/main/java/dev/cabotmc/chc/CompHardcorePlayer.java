package dev.cabotmc.chc;

public class CompHardcorePlayer implements Comparable<CompHardcorePlayer> {
    public String id;
    public String displayName;
    public double bestScore;
    @Override
    public int compareTo(CompHardcorePlayer o) {
        var x = (CompHardcorePlayer) o;
        var d = (Double) x.bestScore;
        return d.compareTo(bestScore);
    }
    
}
