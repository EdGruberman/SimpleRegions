package edgruberman.bukkit.simpleregions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.simpleregions.util.ChunkCoordinates;

/**
 * Relates a server to world indices
 */
public final class Catalog implements Listener {

    public final Plugin plugin;
    public final RegionRepository repository;
    public final Map<String, Index> worlds = new HashMap<String, Index>();
    public Region serverDefault = null;

    /** CommandSender ("SimpleClassName.Name") to working Region reference for commands */
    private final Map<String, Region> working = new HashMap<String, Region>();

    public Catalog(final Plugin plugin, final RegionRepository repository) {
        this.plugin = plugin;
        this.repository = repository;

        for (final World world : plugin.getServer().getWorlds())
            this.worlds.put(world.getName(), new Index(this, world, repository.loadRegions(world)));

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(final WorldLoadEvent event) {
        this.worlds.put(event.getWorld().getName(), new Index(this, event.getWorld(), this.repository.loadRegions(event.getWorld())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(final ChunkLoadEvent event) {
        this.worlds.get(event.getWorld().getName()).chunkLoaded(event.getChunk());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(final ChunkUnloadEvent event) {
        this.worlds.get(event.getWorld().getName()).chunkUnloaded(event.getChunk());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldUnload(final WorldUnloadEvent event) {
        this.worlds.remove(event.getWorld().getName());
    }

    /**
     * Active regions that contain at least one block of the loaded chunk
     * (Unloaded chunks will not have regions returned)
     *
     * @param location location that identifies chunk
     * @return active regions for loaded chunks that contain location; empty set if no regions apply
     * TODO regionsForLoadedChunk(chunkX, chunkZ)
     */
    public Set<Region> getChunkRegions(final Location location) {
        final Set<Region> possible = this.worlds.get(location.getWorld().getName()).loaded.get(ChunkCoordinates.hash(location.getBlockX() >> 4, location.getBlockZ() >> 4));
        return (possible != null ? possible : Collections.<Region>emptySet());
    }

    /**
     * Active regions that contain the specified location in a loaded chunk.
     * If no regions apply, world default is supplied.  If no world default
     * server default is supplied.
     * (Unloaded chunks will not have regions returned)
     *
     * @param location contained by regions
     * @return regions containing location
     * TODO regionsForLoadedLocation(location) remove default add
     */
    public Set<Region> getRegions(final Location location) {
        final Set<Region> regions = new HashSet<Region>();
        for (final Region region : this.getChunkRegions(location))
            if (region.contains(location)) regions.add(region);

        if (regions.size() == 0) {
            final Region def = this.getDefault(location.getWorld().getName());
            if (def != null) regions.add(def);
        }

        return regions;
    }

    /**
     * Determines default applicable region for specified world. A world's
     * default region (non-null) overrides the server default region.
     *
     * @param world world name to return applicable default region for
     * @return default region applicable for world
     * TODO defaultRegion
     */
    public Region getDefault(final String world) {
        final Index index = this.worlds.get(world);
        if (index == null) return this.serverDefault;

        final Region def = index.worldDefault;
        if (def == null) return this.serverDefault;

        return def;
    }

    public Region getRegion(final String region, final String world) {
        if (region.equalsIgnoreCase(Region.NAME_DEFAULT))
            return this.getDefault(world);

        if (world == null) return null;

        return this.worlds.get(world).regions.get(region.toLowerCase());
    }

    /**
     * Determine if player is allowed to manipulate the target location based
     * on region configuration.<br>
     * Access is determined by:<br>
     *     = true if any active region applies and allows access to the player<br>
     *     = true if no active region applies and the world default region allows access to the player<br>
     *     = true if no active region applies and no world default region applies and the server default region allows access to the player<br>
     *     = false otherwise
     *
     * @param player player to check access for
     * @param target block to determine if player has access to
     * @return true if player has access, otherwise false
     */
    public boolean isAllowed(final Player player, final Location target) {
        boolean found = false;

        // Check loaded regions.
        for (final Region region : this.getChunkRegions(target))
            if (region.contains(target)) {
                if (region.hasAccess(player)) return true;
                found = true;
            }

        // If at least one loaded region was found, that indicates all applicable regions would have been found in loaded.
        if (found) return false;

        // Check all regions only if chunk is not loaded at target.
        // Slightly redundant in checking loaded regions again, but this should be a rare edge case.
        if (!target.getWorld().isChunkLoaded(target.getBlockX() >> 4, target.getBlockZ() >> 4)) {
            for (final Region region : this.worlds.get(target.getWorld().getName()).regions.values())
                if (region.isActive() && region.contains(target)) {
                    if (region.hasAccess(player)) return true;
                    found = true;
                }

            // If we found at least one applicable unloaded region, do not check default regions.
            if (found) return false;
        }

        // Check world default region only if no other regions apply.
        final Region worldDefault = this.worlds.get(target.getWorld().getName()).worldDefault;
        if (worldDefault != null && worldDefault.isActive())
            return this.worlds.get(target.getWorld()).worldDefault.hasAccess(player);

        // Check server default region only if no other regions apply and there is no world default region.
        if (this.serverDefault != null && this.serverDefault.isActive())
            return this.serverDefault.hasAccess(player);

        return false;
    }

    public void addRegion(final Region region) {
        this.worlds.get(region.world.getName()).add(region);
    }

    public void removeRegion(final Region region) {
        final Index index = this.worlds.get(region.world.getName());
        if (index == null) throw new IllegalArgumentException("Unable to remove region; region world index not found: " + region.world.getName());

        index.remove(region);
    }

    public void setWorkingRegion(final CommandSender sender, final Region region) {
        this.working.put(sender.getClass().getSimpleName() + "." + sender.getName(), region);
    }

    public void unsetWorkingRegion(final CommandSender sender) {
        this.working.remove(sender.getClass().getSimpleName() + "." + sender.getName());
    }

    public Region getWorkingRegion(final CommandSender sender) {
        final Region region = this.working.get(sender.getClass().getSimpleName() + "." + sender.getName());
        if (region != null) return region;

        if (!(sender instanceof Player)) return null;

        final Set<edgruberman.bukkit.simpleregions.Region> regions = this.getRegions(((Player) sender).getLocation());
        if (regions.size() != 1) return null;

        return regions.iterator().next();
    }

}
