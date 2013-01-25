package edgruberman.bukkit.simpleregions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.World;

import edgruberman.bukkit.simpleregions.util.ChunkCoordinates;

/** relates worlds and chunks to regions */
public final class Index {

    public final World world;

    /** default region to use when no other world regions apply */
    public final Region worldDefault;

    /** all regions in any state for this world */
    public final Set<Region> regions = new HashSet<Region>();

    /** active regions in loaded chunks keyed by chunk hash */
    public final Map<Long, Set<Region>> cache = new HashMap<Long, Set<Region>>();

    /** count of chunks loaded by region */
    private final Map<Region, Long> references = new HashMap<Region, Long>();

    private final Catalog catalog;

    Index(final World world, final Catalog catalog) {
        this.world = world;
        this.catalog = catalog;
        this.worldDefault = catalog.repository.loadDefault(world.getName());
        for (final Region region : catalog.repository.loadRegions(world.getName())) this.register(region);
    }

    public void clear() {
        this.regions.clear();
        this.cache.clear();
    }

    public void register(final Region region) {
        this.regions.add(region);
        this.references.put(region, 0L);
        if (!region.active) return;

        // load region into applicable loaded chunk index entries
        for (final ChunkCoordinates coords : region.chunks())
            if (this.world.isChunkLoaded(coords.x, coords.z))
                this.cache(coords.getHash(), region);
    }

    public void deregister(final Region region) {
        this.regions.remove(region);
        this.references.remove(region);
        this.catalog.deregisterOptions(region);

        // unload region from any loaded chunk index entries
        final Iterator<Entry<Long, Set<Region>>> it = this.cache.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<Long, Set<Region>> next = it.next();
            if (next.getValue().remove(region))
                if (next.getValue().size() == 0)
                    it.remove();
        }
    }

    /** refresh chunk index entries for region; critical to do whenever region definition changes (active or coordinates) */
    public void refresh(final Region region) {
        this.deregister(region);
        this.register(region);
    }

    /** create an index entry to any region containing the loaded chunk */
    void load(final Chunk chunk) {
        this.load(chunk.getX(), chunk.getZ());
    }

    /** create an index entry to any region containing the loaded chunk */
    void load(final int chunkX, final int chunkZ) {
        final long hash = ChunkCoordinates.hash(chunkX, chunkZ);
        for (final Region region : this.regions)
            if (region.isDefined() && region.within(chunkX, chunkZ) && region.active)
                this.cache(hash, region);
    }

    /** remove any index entries to any regions that contain the unloaded chunk */
    void unload(final Chunk chunk) {
        final Set<Region> unloaded = this.cache.remove(ChunkCoordinates.hash(chunk));
        if (unloaded == null) return;

        for (final Region region : unloaded) {
            final long remaining = this.references.get(region);
            this.references.put(region, remaining - 1);
            if (remaining == 1) this.catalog.deregisterOptions(region);
        }
    }

    private void cache(final long chunk, final Region region) {
        final long existing = this.references.put(region, this.references.get(region) + 1);
        if (existing == 0) this.catalog.registerOptions(region);

        Set<Region> loaded = this.cache.get(chunk);
        if (loaded == null) loaded = new HashSet<Region>();
        if (!loaded.add(region)) return;

        if (loaded.size() == 1) this.cache.put(chunk, loaded);
    }

    public Set<Region> cached(final int chunkX, final int chunkZ) {
        final Set<Region> applicable = this.cache.get(ChunkCoordinates.hash(chunkX, chunkZ));
        return (applicable != null ? applicable : Collections.<Region>emptySet());
    }

}
