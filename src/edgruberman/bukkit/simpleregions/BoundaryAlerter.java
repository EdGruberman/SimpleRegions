package edgruberman.bukkit.simpleregions;

import java.util.HashMap;
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
    private final Map<String, Location> lastBlockChange = new HashMap<String, Location>();

    BoundaryAlerter(final Catalog catalog) {
        this.catalog = catalog;
        for (final Player player : Bukkit.getOnlinePlayers()) this.lastBlockChange.put(player.getName(), player.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.lastBlockChange.put(event.getPlayer().getName(), event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Location last = this.lastBlockChange.get(event.getPlayer().getName());
        final Location to = event.getTo();

        // only check further on block transitions
        if ((last.getBlockX() == to.getBlockX()) && (last.getBlockZ() == to.getBlockZ()) && (last.getBlockY() == to.getBlockY())) return;

        this.lastBlockChange.put(event.getPlayer().getName(), to);
        this.checkCrossings(event.getPlayer(), last, to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.lastBlockChange.remove(event.getPlayer().getName());
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

        // filter applicable regions to check by chunk
        final Set<Region> regions = this.catalog.cached(from.getWorld(), from.getBlockX() >> 4, from.getBlockZ() >> 4);
        if (!BoundaryAlerter.sameChunk(from, to)) regions.addAll(this.catalog.cached(to.getWorld(), to.getBlockX() >> 4, to.getBlockZ() >> 4));

        boolean isInFrom, isInTo;
        for (final Region region : regions) {
            // determine if this region boundary has been crossed
            isInFrom = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
            isInTo = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());
            if (isInFrom == isInTo) continue;

            // exiting this region, show farewell first
            if (isInFrom)
                if (region.farewell == null || region.farewell.length() > 0)
                    Main.courier.send(player, ( region.greeting == null ? "farewell-default" : "farewell-custom" ), region.name, region.hasAccess(player)?1:0, region.farewell);

            // entering this region, cache greeting for display after all other farewells
            if (isInTo)
                if (region.greeting == null || region.greeting.length() > 0) {
                    final Message greeting = Main.courier.compose(( region.greeting == null ? "greeting-default" : "greeting-custom" ), region.name, region.hasAccess(player)?1:0, region.greeting);
                    if (entered == null) { entered = greeting; } else { entered.append(greeting); }
                }
        }

        // show any greetings only after farewells
        if (entered != null) Main.courier.submit(new Individual(player), entered);
    }

    private static boolean sameChunk(final Location i, final Location j) {
        return ((i.getBlockX() >> 4) == (j.getBlockX() >> 4)) && ((i.getBlockZ() >> 4) == (j.getBlockZ() >> 4));
    }

}
