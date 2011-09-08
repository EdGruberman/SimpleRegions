package edgruberman.bukkit;

import org.bukkit.Location;

/**
 * Pair of coordinates and utility methods for hashing chunk coordinates.
 */
public class ChunkCoordinates {

    /**
     * Biject through combination.
     */
    public static long hash(int x, int z) {
        return ((long) x << 32) + z - Integer.MIN_VALUE;
    }
    
    public static int x(long hash) {
        return (int) (hash >> 32);
    }
    
    public static int z(long hash) {
        return (int) (hash & 0xFFFFFFFF) + Integer.MIN_VALUE;
    }
    
    public final int x;
    public final int z;
    
    private Long hash = null;
    
    public ChunkCoordinates(final int x, final int z) {
        this.x = x;
        this.z = z;
    }
    
    public ChunkCoordinates(final Location loc) {
        this.x = loc.getBlockX() >> 4;
        this.z = loc.getBlockZ() >> 4;
    }
    
    /**
     * Calculates and caches on the first call.  Subsequent calls use cache.
     * 
     * @return
     */
    public long getHash() {
        if (this.hash == null) this.hash = ChunkCoordinates.hash(this.x, this.z);
        return this.hash;
    }
    
    @Override public String toString() {
        return "(x:" + this.x + " z:" + this.z + ")";
    }
    
    /**
     * Mimicing from nms.ChunkCoordIntPair
     * TODO figure out exactly what this does and if it's possible to simplify/eliminate
     */
    @Override
    public int hashCode() {
        return (this.x < 0 ? Integer.MIN_VALUE : 0) | (this.x & 32767) << 16 | (this.z < 0 ? '\u8000' : 0) | this.z & 32767;
    }
    
    public boolean equals(Object other) {
        ChunkCoordinates that = (ChunkCoordinates) other;
        return (this.x == that.x) && (this.z == that.z);
    }
}