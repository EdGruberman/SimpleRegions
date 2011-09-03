package edgruberman.bukkit.simpleregions;

import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.ChunkCoordinates;

/**
 * Manages world region index updates.
 */
public class IndexPublisher extends WorldListener {
    
    IndexPublisher(final Plugin plugin) {
        for (World world : plugin.getServer().getWorlds())
            new Index(world);
        
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.CHUNK_LOAD, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.CHUNK_UNLOAD, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.WORLD_LOAD, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.WORLD_UNLOAD, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onChunkLoad(final ChunkLoadEvent event) {
        Index index = Index.worlds.get(event.getWorld());
        for (Region region : index.regions)
            if (region.isActive() && region.within(event.getChunk().getX(), event.getChunk().getZ())) {
                ChunkCoordinates coords = new ChunkCoordinates(event.getChunk().getX(), event.getChunk().getZ());
                if (!index.loaded.containsKey(coords.getHash())) index.loaded.put(coords.getHash(), new HashSet<Region>());
                index.loaded.get(coords.getHash()).add(region);
            }
    }
    
    @Override
    public void onChunkUnload(final ChunkUnloadEvent event) {
        Index index = Index.worlds.get(event.getWorld());
        index.loaded.remove(ChunkCoordinates.hash(event.getChunk().getX(), event.getChunk().getZ()));
    }
    
    @Override
    public void onWorldLoad(final WorldLoadEvent event) {
        new Index(event.getWorld());
    }
    
    @Override
    public void onWorldUnload(final WorldUnloadEvent event) {
        Index.worlds.remove(event.getWorld());
    }
}