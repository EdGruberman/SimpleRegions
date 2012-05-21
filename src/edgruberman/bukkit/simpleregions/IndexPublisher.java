package edgruberman.bukkit.simpleregions;

import java.util.HashSet;

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
 * Manages world region index updates.
 */
public class IndexPublisher implements Listener {

    IndexPublisher(final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(final ChunkLoadEvent event) {
        final Index index = Index.worlds.get(event.getWorld());
        for (final Region region : index.regions.values())
            if (region.isActive() && region.within(event.getChunk().getX(), event.getChunk().getZ())) {
                final ChunkCoordinates coords = new ChunkCoordinates(event.getChunk().getX(), event.getChunk().getZ());
                if (!index.loaded.containsKey(coords.getHash())) index.loaded.put(coords.getHash(), new HashSet<Region>());
                index.loaded.get(coords.getHash()).add(region);
            }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(final ChunkUnloadEvent event) {
        final Index index = Index.worlds.get(event.getWorld());
        index.loaded.remove(ChunkCoordinates.hash(event.getChunk().getX(), event.getChunk().getZ()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(final WorldLoadEvent event) {
        new Index(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(final WorldUnloadEvent event) {
        Index.worlds.remove(event.getWorld());
    }

}
