package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

final class BoundaryAlerter implements Listener {

    private final Catalog catalog;
    private final Map<Player, Location> lastBlockChange = new HashMap<Player, Location>();

    BoundaryAlerter(final Catalog catalog) {
        this.catalog = catalog;
        catalog.plugin.getServer().getPluginManager().registerEvents(this, catalog.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.lastBlockChange.put(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Location last = this.lastBlockChange.get(event.getPlayer());
        final Location to = event.getTo();

        // Only check further on block transitions
        if ((last.getBlockX() == to.getBlockX()) && (last.getBlockZ() == to.getBlockZ()) && (last.getBlockY() == to.getBlockY())) return;

        this.lastBlockChange.put(event.getPlayer(), to);
        this.checkCrossings(event.getPlayer(), last, to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
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
    private void checkCrossings(final Player player, final Location from, final Location to) {
        List<String> exited = null, enteredFormat = null, enteredMessage = null;

        // Determine applicable regions to check
        final Set<Region> regions = new HashSet<Region>();
        regions.addAll(this.catalog.getChunkRegions(from));
        if (!BoundaryAlerter.sameChunk(from, to)) regions.addAll(this.catalog.getChunkRegions(to));

        boolean isInFrom, isInTo;
        for (final Region region : regions) {
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
                if (enteredFormat == null) {
                    enteredFormat = new ArrayList<String>();
                    enteredMessage = new ArrayList<String>();
                }
                enteredFormat.add(Main.messenger.getFormat(region.hasAccess(player) ? "enterHasAccess" : "enterNoAccess"));
                enteredMessage.add(region.enter.formatted);
            }
        }

        // Show any exit messages first
        if (exited != null)
            for (final String message : exited)
                Main.messenger.tell(player, "exit", message);

        // Show any enter messages next
        if (enteredFormat != null)
            for (int i = 0; i <= enteredFormat.size() - 1; i++) {
                final String message = Main.messenger.tellMessage(player, enteredFormat.get(i), enteredMessage.get(i));
                this.catalog.plugin.getLogger().finer("#TELL@" + player.getName() + "# " + message);
            }
    }

    private static boolean sameChunk(final Location i, final Location j) {
        return ((i.getBlockX() >> 4) == (j.getBlockX() >> 4)) && ((i.getBlockZ() >> 4) == (j.getBlockZ() >> 4));
    }

}
