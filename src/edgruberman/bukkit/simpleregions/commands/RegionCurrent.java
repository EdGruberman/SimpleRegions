package edgruberman.bukkit.simpleregions.commands;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;

// Syntax: /region[ current][ <Player>]
public class RegionCurrent extends Action {

    public static final String NAME = "current";

    private final Region base;

    RegionCurrent(final Region owner) {
        super(owner, RegionCurrent.NAME, Permission.REGION_CURRENT);
        this.base = owner;
    }

    @Override
    void execute(final Context context) {
        this.message(context, null);
    }

    void message(final Context context, final Location location) {
        final String name = RegionCurrent.parsePlayerName(context);
        if (name == null) {
            context.respond("Unable to determine target player name.", MessageLevel.SEVERE);
            return;
        }

        Location target = location;
        String world = null;

        // If a specific location is not specified, use target player's current location
        if (location == null) {
            final Player player = Region.getExactPlayer(name);
            if (player == null) {
                context.respond("Unable to find target player \"" + name + "\"", MessageLevel.SEVERE);
                return;
            }

            target = player.getLocation();
            world = player.getWorld().getName();
        }

        // Get applicable regions
        final Set<edgruberman.bukkit.simpleregions.Region> regions = this.base.catalog.getRegions(target);

        // Compile region name list
        String names = "";
        for (final edgruberman.bukkit.simpleregions.Region region : regions) {
            if (names.length() != 0) names += ", ";
            names += region.getDisplayName();
        }

        // Compile response message
        String message = "Current region" + (regions.size() > 1 ? "s" : "");
        if (context.player == null || !name.equals(context.player.getName())) message += " for " + (world != null ? "[" + world + "] " : "") + name;
        if (location != null) message += " at (x:" + target.getBlockX() + " y:" + target.getBlockY() + " z:" + target.getBlockZ() + ")";

        message += ": ";
        if (regions.size() == 0) {
            message += "(none)";
        } else {
            message += names;
        }

        // Determine response level based on target access
        MessageLevel level = MessageLevel.STATUS;
        if (!this.base.catalog.isAllowed(name, target))
            level = MessageLevel.WARNING;

        context.respond(message, level);
    }

    private static String parsePlayerName(final Context context) {
        if (context.arguments.size() <= (context.actionIndex + 1))
            return (context.player == null ? null : context.player.getName());

        return context.arguments.get(context.actionIndex + 1);
    }

}
