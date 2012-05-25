package edgruberman.bukkit.simpleregions.util;

import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 * Pair of coordinates and utility methods for hashing chunk coordinates.
 */
public class ChunkCoordinates {

    public static long hash(final Chunk chunk) {
        return ChunkCoordinates.hash(chunk.getX(), chunk.getZ());
    }

    /**
     * Biject through combination.
     */
    public static long hash(final int x, final int z) {
        return ((long) x << 32) + z - Integer.MIN_VALUE;
    }

    public static int x(final long hash) {
        return (int) (hash >> 32);
    }

    public static int z(final long hash) {
        return (int) (hash & 0xFFFFFFFF) + Integer.MIN_VALUE;
    }

    public final int x;
    public final int z;

    private Long hash = null;

    public ChunkCoordinates(final Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

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

    @Override
    public boolean equals(final Object other) {
        final ChunkCoordinates that = (ChunkCoordinates) other;
        return (this.x == that.x) && (this.z == that.z);
    }
}