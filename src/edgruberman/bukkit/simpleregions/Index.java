package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import edgruberman.bukkit.ChunkCoordinates;
import edgruberman.bukkit.messagemanager.MessageLevel;

/**
 * Region to world reference which also correlates loaded chunks to active regions.
 */
public final class Index {
    
    public static Map<World, Index> worlds = new HashMap<World, Index>();
    public static Region serverDefault = null;
    
    public Region worldDefault = null;
    public Set<Region> regions = new HashSet<Region>();
    
    Map<Long, Set<Region>> loaded = new HashMap<Long, Set<Region>>();
    
    /**
     * Currently loaded regions that are inside the chunk of the specified location.
     * 
     * @param target location to return loaded regions for
     * @return loaded regions that contain location, empty list if none apply
     */
    public static Set<Region> getChunkRegions(final Location target) {
        Set<Region> possible = Index.worlds.get(target.getWorld()).loaded.get(ChunkCoordinates.hash(target.getBlockX() >> 4, target.getBlockZ() >> 4));
        return (possible != null ? possible : Collections.<Region>emptySet());
    }
    
    static boolean add(final Region region) {
        if (!Index.isUnique(region)) {
            Main.messageManager.log("Unable to index region \"" + region.getName() + "\" in world [" + region.getWorld().getName() + "]; Name not unique.", MessageLevel.WARNING);
            return false;
        }
        
        // Add as a default region
        if (region.getName() == null) {
            if (region.getWorld() == null) {
                Index.serverDefault = region;
                return true;
            }
            
            Index.worlds.get(region.getWorld()).worldDefault = region;
            return true;
        }
        
        // Add as a world region
        Index index = Index.worlds.get(region.getWorld());
        if (!index.regions.add(region)) return false;
        
        // Inactive regions have nothing left to do here
        if (!region.isActive()) return true;
        
        // Populate loaded chunk index
        for (ChunkCoordinates coords : Index.chunks(region))
            if (region.getWorld().isChunkLoaded(coords.x, coords.z)) {
                if (!index.loaded.containsKey(coords.getHash())) index.loaded.put(coords.getHash(), new HashSet<Region>());
                index.loaded.get(coords.getHash()).add(region);
            }
        
        return true;
    }
    
    static boolean refresh(final Region region) {
        if (!Index.remove(region)) return false;
        
        return Index.add(region);
    }
    
    static boolean remove(final Region region) {
        Index index = Index.worlds.get(region.getWorld());
        if (!index.regions.remove(region)) return false;
        
        for (ChunkCoordinates coords : Index.chunks(region)) {
            if (!index.loaded.containsKey(coords.getHash())) continue;
            
            index.loaded.get(coords.getHash()).remove(region);
            if (index.loaded.get(coords.getHash()).size() == 0)
                index.loaded.remove(coords.getHash());
        }
        
        return true;
    }
    
    /**
     * There can be only one! (name per world anyways)
     * 
     * @param region region to check if unique
     * @return true if the region name is unique for the world; otherwise false
     */
    static boolean isUnique(final Region region) {
        return Index.worlds.get(region.getWorld()).regions.contains(region);
    }
    
    /**
     * Calculate list of chunks that contain at least one block of the region.
     * 
     * @param region region to calculate chunks for
     * @return all chunk coordinates that contain a part of region
     */
    private static List<ChunkCoordinates> chunks(final Region region) {
        if (!region.isDefined()) return Collections.<ChunkCoordinates>emptyList();
        
        List<ChunkCoordinates> coords = new ArrayList<ChunkCoordinates>();
        for (int x = region.getN(); x <= region.getS(); x++)
            for (int z = region.getE(); z <= region.getW(); z++)
                coords.add(new ChunkCoordinates(x, z));
        
        return coords;
    }
    
    /**
     * Create a new index and load all related regions.
     * 
     * @param world world to index regions for
     */
    Index(final World world) {
        Index.worlds.put(world, this);
        Main.loadRegions(world);
    }
}