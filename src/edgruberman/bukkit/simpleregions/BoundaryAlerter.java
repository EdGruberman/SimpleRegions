package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class BoundaryAlerter extends PlayerListener {
    
    private Map<Player, Location> lastBlockChange = new HashMap<Player, Location>();
    
    BoundaryAlerter(final Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.lastBlockChange.put(event.getPlayer(), event.getPlayer().getLocation());
    }
    
    @Override
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        Location last = this.lastBlockChange.get(event.getPlayer());
        Location to = event.getTo();
        
        // Only check further on block transitions.
        if ((last.getBlockX() == to.getBlockX()) && (last.getBlockZ() == to.getBlockZ()) && (last.getBlockY() == to.getBlockY())) return;
        
        this.lastBlockChange.put(event.getPlayer(), to);
        
        BoundaryAlerter.checkCrossings(event.getPlayer(), last, to);
    }
    
    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.lastBlockChange.remove(event.getPlayer());
    }
    
    /**
     * Determines if player has crossed an active region boundary and
     * displays a message to the player if configured.
     *  
     * @param player player whose movement is to be checked
     * @param from last block player was seen at before to
     * @param to block player has moved to
     */
    private static void checkCrossings(final Player player, final Location from, final Location to) {
        List<String> exited = null, entered = null;
        List<MessageLevel> enteredLevel = null;
        
        // Determine applicable regions to check
        Set<Region> regions = new HashSet<Region>();
        regions.addAll(Index.getChunkRegions(from));
        if (!BoundaryAlerter.isChunkEquals(from, to)) regions.addAll(Index.getChunkRegions(to));
        
        boolean isInFrom, isInTo;
        for (Region region : regions) {
            // Determine if this region's boundary has been crossed
            isInFrom = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
            isInTo = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());
            if (isInFrom == isInTo) continue;
            
            // Exiting this region
            if (isInFrom && region.exit.formatted.length() != 0) {
                if (exited == null) exited = new ArrayList<String>();
                exited.add(region.exit.formatted);
            }
            
            // Entering this region
            if (isInTo && region.enter.formatted.length() != 0) {
                if (entered == null) entered = new ArrayList<String>();
                if (enteredLevel == null) enteredLevel = new ArrayList<MessageLevel>();
                entered.add(region.enter.formatted);
                enteredLevel.add((region.access.isAllowed(player.getName()) ? MessageLevel.STATUS : MessageLevel.WARNING));
            }
        }
        
        // Show any exit messages first
        if (exited != null)
            for (String message : exited)
                Main.messageManager.send(player, message, MessageLevel.STATUS);
        
        // Show any enter messages next
        if (entered != null)
            for (int i = 0; i <= entered.size() - 1; i++)
                Main.messageManager.send(player, entered.get(i), enteredLevel.get(i));
    }
    
    private static boolean isChunkEquals(final Location i, final Location j) {
        return ((i.getBlockX() >> 4) == (j.getBlockX() >> 4)) && ((i.getBlockZ() >> 4) == (j.getBlockZ() >> 4));
    }
}