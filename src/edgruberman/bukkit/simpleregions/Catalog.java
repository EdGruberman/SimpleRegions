package edgruberman.bukkit.simpleregions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.simpleregions.util.ChunkCoordinates;

/** relates a server to world indices */
public final class Catalog implements Listener {

    public final Plugin plugin;
    public final Repository repository;
    public Region serverDefault;

    /** world to region index keyed by world name */
    public final Map<String, Index> indices = new HashMap<String, Index>();

    Catalog(final Plugin plugin, final Repository repository) {
        this.plugin = plugin;
        this.repository = repository;
        this.serverDefault = repository.loadDefault(null);
        for (final World world : plugin.getServer().getWorlds()) this.loadIndex(world);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void clear() {
        HandlerList.unregisterAll(this);
        this.repository.clear();
        this.indices.clear();
    }

    private void loadIndex(final World world) {
        final Index index = new Index(world, this.repository);
        this.indices.put(world.getName(), index);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(final WorldLoadEvent event) {
        this.loadIndex(event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldUnload(final WorldUnloadEvent event) {
        this.indices.remove(event.getWorld().getName());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChunkLoad(final ChunkLoadEvent event) {
        this.indices.get(event.getWorld().getName()).loadChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChunkUnload(final ChunkUnloadEvent event) {
        this.indices.get(event.getWorld().getName()).unloadChunk(event.getChunk());
    }

    /**
     * @param location location that identifies chunk
     * @return active regions that contain at least one block for the loaded chunk that contains location; empty set if no regions apply
     */
    public Set<Region> getChunkRegions(final Location location) {
        final Set<Region> possible = this.indices.get(location.getWorld().getName()).loaded.get(ChunkCoordinates.hash(location.getBlockX() >> 4, location.getBlockZ() >> 4));
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
            final Region def = this.defaultFor(location.getWorld().getName());
            if (def != null) regions.add(def);
        }

        return regions;
    }

    /** @return world default region if exists; otherwise returns server default region; null if neither exist */
    public Region defaultFor(final String world) {
        final Index index = this.indices.get(world);
        if (index == null) return this.serverDefault;

        final Region def = index.worldDefault;
        if (def == null) return this.serverDefault;

        return def;
    }

    /**
     * Determine if player is allowed to manipulate the target location based on region configuration.<br>
     * <br>
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
        if (player.hasPermission("simpleregions.override.protection")) return true;

        boolean found = false;

        // check loaded regions, return true if any region allows access
        for (final Region region : this.getChunkRegions(target))
            if (region.contains(target)) {
                if (region.hasAccess(player)) return true;
                found = true;
            }

        // if at least one loaded region was found, that indicates all applicable regions would have been found in loaded
        if (found) return false;

        // check world default region only if no other regions apply
        final Region worldDefault = this.indices.get(target.getWorld().getName()).worldDefault;
        if (worldDefault != null && worldDefault.active)
            return this.indices.get(target.getWorld()).worldDefault.hasAccess(player);

        // check server default region only if no other regions apply and there is no world default region
        if (this.serverDefault != null && this.serverDefault.active)
            return this.serverDefault.hasAccess(player);

        return false;
    }

}
