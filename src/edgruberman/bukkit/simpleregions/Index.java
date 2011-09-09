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

    World world;
    Map<String, Region> regions = new HashMap<String, Region>();
    Map<Long, Set<Region>> loaded = new HashMap<Long, Set<Region>>();
    
    /**
     * Currently active and loaded regions that are inside the chunk of the
     * specified location. (This should only be used to check actions being
     * performed by players against loaded chunks.  Unloaded chunks will
     * not have regions returned from this function.)
     * 
     * @param target location to return loaded regions for
     * @return loaded regions that contain location, empty list if none apply
     */
    public static Set<Region> getChunkRegions(final Location target) {
        Set<Region> possible = Index.worlds.get(target.getWorld()).loaded.get(ChunkCoordinates.hash(target.getBlockX() >> 4, target.getBlockZ() >> 4));
        return (possible != null ? possible : Collections.<Region>emptySet());
    }
    
    /**
     * Currently active and loaded regions that contain the specified target.
     * If no regions apply, world default is supplied.  If no world default
     * server default is supplied.
     * 
     * @param target contained by regions
     * @return regions containing target
     */
    public static Set<Region> getRegions(final Location target) {
        Set<Region> regions = new HashSet<Region>();
        for (Region region : Index.getChunkRegions(target))
            if (region.contains(target)) regions.add(region);
        
        if (regions.size() == 0) {
            Region def = Index.getDefault(target.getWorld());
            if (def != null) regions.add(def);
        }
        
        return regions;
    }
    
    /**
     * Determines default applicable region for specified world. A world's
     * default region (non-null) overrides the server default region.
     * 
     * @param world world to return applicable default region for
     * @return default region applicable for world
     */
    public static Region getDefault(final World world) {
        Region def = Index.worlds.get(world).worldDefault;
        if (def != null) return def;
        
        return Index.serverDefault;
    }
    
    static boolean add(final Region region) {
        // Add as a default region
        if (region.getName() == null) {
            if (region.getWorld() == null) {
                Index.serverDefault = region;
                return true;
            }
            
            Index.worlds.get(region.getWorld()).worldDefault = region;
            return true;
        }
        
        // Ensure the region can be uniquely identified
        if (Index.exists(region)) {
            Main.messageManager.log("Unable to index region \"" + region.getName() + "\" in world [" + region.getWorld().getName() + "]; Name not unique.", MessageLevel.WARNING);
            return false;
        }
        
        // Add as a world region
        Index index = Index.worlds.get(region.getWorld());
        index.regions.put(region.getName(), region);
        
        // Inactive regions have nothing left to do here
        if (!region.isActive()) return true;
        
        // Populate loaded chunk index for active regions
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
    
    public static boolean remove(final Region region) {
        // Remove default regions only if they match the requested one.
        if (region.getName() == null) {
            if (region.getWorld() == null) {
                if (Index.serverDefault != null && !Index.serverDefault.equals(region)) return false;
                
                Index.serverDefault = null;
                return true;
            }
            
            if (Index.worlds.get(region.getWorld()).worldDefault != null && Index.worlds.get(region.getWorld()).worldDefault.equals(region)) return false;
            
            Index.worlds.get(region.getWorld()).worldDefault = null;
            return true;
        }
        
        // Remove the region itself
        Index index = Index.worlds.get(region.getWorld());
        if (!index.regions.containsKey(region.getName())) return false;
        index.regions.remove(region.getName());
        
        // Remove chunk references to region
        for (ChunkCoordinates coords : Index.chunks(region)) {
            if (!index.loaded.containsKey(coords.getHash())) continue;
            
            index.loaded.get(coords.getHash()).remove(region);
            
            // Remove the chunk from the index if it's no longer referencing any regions
            if (index.loaded.get(coords.getHash()).size() == 0)
                index.loaded.remove(coords.getHash());
        }
        
        return true;
    }
    
    /**
     * There can be only one! (name per world anyways)
     * 
     * @param region region to check if already exists in index
     * @return true if the region name exists for the world; otherwise false
     */
    static boolean exists(final Region region) {
        return Index.worlds.get(region.getWorld()).regions.containsKey(region.getName());
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
        for (int x = region.getMinChunkX(); x <= region.getMaxChunkX(); x++)
            for (int z = region.getMinChunkZ(); z <= region.getMaxChunkZ(); z++)
                coords.add(new ChunkCoordinates(x, z));
        
        return coords;
    }
    
    /**
     * Create a new index and load all related regions.
     * 
     * @param world world to index regions for
     */
    Index(final World world) {
        this.world = world;
        Index.worlds.remove(world);
        Index.worlds.put(world, this);
        Main.loadRegions(world);
    }
    
    public World getWorld() {
        return this.world;
    }
    
    /**
     * All regions for this index.
     * 
     * @return unmodifiable set of regions
     */
    public Map<String, Region> getRegions() {
        return Collections.unmodifiableMap(this.regions);
    }
}