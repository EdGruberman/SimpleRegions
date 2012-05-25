package edgruberman.bukkit.simpleregions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.World;

import edgruberman.bukkit.simpleregions.util.ChunkCoordinates;

/**
 * Relates a world to regions
 */
public final class Index {

    public final Catalog catalog;
    public final World world;

    /**
     * Regions by name (lower case)
     */
    public final Map<String, Region> regions = new HashMap<String, Region>();

    /**
     * Regions by chunk (hash)
     */
    public final Map<Long, Set<Region>> loaded = new HashMap<Long, Set<Region>>();

    /**
     * Default region to use when no other world regions apply
     */
    public Region worldDefault = null;

    Index(final Catalog catalog, final World world, final Collection<Region> regions) {
        this.catalog = catalog;
        this.world = world;
        for (final Region region : regions) this.add(region);
    }

    void add(final Region region) {
        this.regions.put(region.getName().toLowerCase(), region);
        region.worldIndex = this;
        if (!region.isActive()) return;

        // Populate loaded chunk index for active regions
        for (final ChunkCoordinates coords : region.chunks())
            if (this.world.isChunkLoaded(coords.x, coords.z))
                this.chunkLoaded(coords.x, coords.z);
    }

    void remove(final Region region) {
        if (this.regions.remove(region.getName().toLowerCase()) == null) return;

        region.worldIndex = null;
        if (!region.isActive()) return;

        // Remove chunk references to region
        for (final ChunkCoordinates coords : region.chunks()) {
            final Set<Region> loaded = this.loaded.get(coords.getHash());
            if (loaded == null) continue;

            loaded.remove(region);
            if (loaded.size() == 0) this.loaded.remove(coords.getHash());
        }
    }

    void refresh(final Region region) {
        this.remove(region);
        this.add(region);
    }

    /**
     * Store a pointer to any region containing the chunk
     */
    void chunkLoaded(final Chunk chunk) {
        this.chunkLoaded(chunk.getX(), chunk.getZ());
    }

    /**
     * Store a pointer to any region containing the chunk
     */
    void chunkLoaded(final int chunkX, final int chunkZ) {
        final long hash = ChunkCoordinates.hash(chunkX, chunkZ);
        for (final Region region : this.regions.values()) {
            if (!region.isActive() || !region.within(chunkX, chunkZ)) continue;

            Set<Region> loaded = this.loaded.get(hash);
            if (loaded == null) loaded = new HashSet<Region>();
            if (!loaded.add(region)) continue;

            if (loaded.size() == 1) this.loaded.put(hash, loaded);
        }
    }

    /**
     * Remove any pointer to any regions that contain the chunk
     */
    void chunkUnloaded(final Chunk chunk) {
        this.loaded.remove(ChunkCoordinates.hash(chunk));
    }

}
