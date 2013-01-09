package edgruberman.bukkit.simpleregions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import edgruberman.bukkit.simpleregions.messaging.Individual;
import edgruberman.bukkit.simpleregions.messaging.Message;

public final class BoundaryAlerter implements Listener {

    private final Catalog catalog;
    private final Map<Player, Location> lastBlockChange = new HashMap<Player, Location>();

    BoundaryAlerter(final Catalog catalog) {
        this.catalog = catalog;
        for (final Player player : Bukkit.getOnlinePlayers()) this.lastBlockChange.put(player, player.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.lastBlockChange.put(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Location last = this.lastBlockChange.get(event.getPlayer());
        final Location to = event.getTo();

        // only check further on block transitions
        if ((last.getBlockX() == to.getBlockX()) && (last.getBlockZ() == to.getBlockZ()) && (last.getBlockY() == to.getBlockY())) return;

        this.lastBlockChange.put(event.getPlayer(), to);
        this.checkCrossings(event.getPlayer(), last, to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.lastBlockChange.remove(event.getPlayer());
    }

    /**
     * determines if player has crossed an active region boundary and
     * displays a message to the player if configured
     *
     * @param player player whose movement is to be checked
     * @param from last block player was seen at before to
     * @param to block player has moved to
     */
    private void checkCrossings(final Player player, final Location from, final Location to) {
        Message entered = null;

        // determine applicable regions to check
        final Set<Region> regions = new HashSet<Region>();
        regions.addAll(this.catalog.getChunkRegions(from));
        if (!BoundaryAlerter.sameChunk(from, to)) regions.addAll(this.catalog.getChunkRegions(to));

        boolean isInFrom, isInTo;
        for (final Region region : regions) {
            // determine if this region boundary has been crossed
            isInFrom = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
            isInTo = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());
            if (isInFrom == isInTo) continue;

            // exiting this region, show message first
            if (isInFrom)
                if (region.exit == null || region.exit.length() > 0)
                    Main.courier.send(player, ( region.enter == null ? "exit" : "exitCustom" ), region.name, region.hasAccess(player)?1:0, region.exit);

            // entering this region, cache message for display after all other exits
            if (isInTo)
                if (region.enter == null || region.enter.length() > 0) {
                    final Message enter = Main.courier.compose(( region.enter == null ? "enter" : "enterCustom" ), region.name, region.hasAccess(player)?1:0, region.enter);
                    if (entered == null) { entered = enter; } else { entered.append(enter); }
                }
        }

        // show any enter messages after exit messages
        if (entered != null) Main.courier.submit(new Individual(player), entered);
    }

    private static boolean sameChunk(final Location i, final Location j) {
        return ((i.getBlockX() >> 4) == (j.getBlockX() >> 4)) && ((i.getBlockZ() >> 4) == (j.getBlockZ() >> 4));
    }

}
