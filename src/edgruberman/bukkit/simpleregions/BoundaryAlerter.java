package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import edgruberman.bukkit.simpleregions.messaging.Message;
import edgruberman.bukkit.simpleregions.messaging.Sender;

public final class BoundaryAlerter implements Listener {

    private final Catalog catalog;
    private final Map<Player, Location> lastBlockChange = new HashMap<Player, Location>();

    BoundaryAlerter(final Catalog catalog) {
        this.catalog = catalog;
        for (final Player player : Bukkit.getOnlinePlayers()) this.lastBlockChange.put(player, player.getLocation());
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
        List<Message> entered = null;

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
            if (isInFrom) Main.courier.submit(new Sender(player), this.draft(region.exit, "exit", "exitCustom", region, player));

            // entering this region, cache message for display after all other exits
            if (isInTo) {
                if (entered == null) entered = new ArrayList<Message>();
                entered.addAll(this.draft(region.enter, "enter", "enterCustom", region, player));
            }
        }

        // show any enter messages after exit messages
        if (entered != null)
            Main.courier.submit(new Sender(player), entered);
    }

    public List<Message> draft(final String custom, final String defaultPath, final String customPath, final Region region, final CommandSender target) {
        if (custom != null && custom.length() == 0) return Collections.emptyList();

        final Object[] arguments = new Object[] { region.name, region.hasAccess(target)?1:0 };

        if (custom != null) {
            // embed custom formatted inside custom message
            final StringBuffer formatted = Message.Factory.create(custom, arguments).build().format(target);
            return Main.courier.draft(customPath, formatted);
        }

        return Main.courier.draft(defaultPath, arguments);
    }

    private static boolean sameChunk(final Location i, final Location j) {
        return ((i.getBlockX() >> 4) == (j.getBlockX() >> 4)) && ((i.getBlockZ() >> 4) == (j.getBlockZ() >> 4));
    }

}
