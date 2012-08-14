package edgruberman.bukkit.simpleregions.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Index;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public abstract class RegionExecutor extends Executor {

    public static final String SERVER_DEFAULT = "SERVER";
    public static final String NAME_DEFAULT = "DEFAULT";

    protected final Catalog catalog;
    protected final Integer index;
    protected final boolean requiresOwner;

    RegionExecutor(final Catalog catalog) {
        this(catalog, null, false);
    }

    /** @param index -1 to force working region; null to not parse region; 0+ to parse region in [Region[ World]] format starting at index */
    RegionExecutor(final Catalog catalog, final Integer index, final boolean requiresOwner) {
        this.catalog = catalog;
        this.index = index;
        this.requiresOwner = requiresOwner;
    }

    abstract boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, Region region);

    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        Region region = null;
        if (this.index != null) {
            region = this.identifyRegion(sender, args);
            if (region == null) return false;

            if (this.requiresOwner && !RegionExecutor.isOwner(sender, region)) {
                Main.courier.send(sender, "requiresOwner", label);
                return true;
            }
        }

        return this.execute(sender, command, label, args, region);
    }

    private Region identifyRegion(final CommandSender sender, final List<String> args) {
        if (this.index == null || args.size() <= this.index || this.index < 0) {
            // <Region> and <World> arguments not supplied

            // prioritize previously set working region
            final Region working = RegionExecutor.getWorkingRegion(sender);
            if (working != null) return working;

            if (!(sender instanceof Player)) return null;

            // assume current region if player is in only one
            final Set<Region> regions = this.catalog.getRegions(((Player) sender).getLocation());
            if (regions.size() != 1) {
                Main.courier.send(sender, "regionNotFound", "");
                return null;
            }

            return regions.iterator().next();
        }

        // console requires both <Region> and <World> arguments
        if (!(sender instanceof Player) && args.size() < this.index + 2) return null;

        // <Region> and <World> arguments both supplied, or <Region> is supplied and sender is Player
        final String region = args.get(this.index);
        final String world = (args.size() >= this.index + 2 ? args.get(this.index + 1) : ((Player) sender).getWorld().getName());

        final Index index = this.catalog.indices.get(world);
        if (index == null) {
            Main.courier.send(sender, "worldNotFound", world);
            return null;
        }

        Region found = null;
        if (region.equalsIgnoreCase(RegionExecutor.NAME_DEFAULT)) {
            found = this.catalog.defaultFor(world.equalsIgnoreCase(RegionExecutor.SERVER_DEFAULT) ? null : world);

        } else {
            final String compare = region.toLowerCase();
            for (final Region r : index.regions)
                if (r.name.toLowerCase().equals(compare)) {
                    found = r;
                    break;
                }
        }

        if (found == null) Main.courier.send(sender, "regionNotFound");
        return found;
    }

    protected static boolean isOwner(final CommandSender sender, final Region region) {
        if (sender.hasPermission("simpleregions.override.commands")) return true;

        if ((sender instanceof Player) && region.owners.inherits(sender)) return true;

        return false;
    }

    public static String formatWorld(final Region region) {
        if (region.world == null) return RegionExecutor.SERVER_DEFAULT;

        String display = region.world;
        if (display.contains(" ")) display = "\"" + display + "\"";
        return display;
    }

    public static String formatName(final Region region) {
        if (region.isDefault()) return RegionExecutor.NAME_DEFAULT;

        String display = region.name;
        if (display.contains(" ")) display = "\"" + display + "\"";

        return display;
    }

    public static String formatNames(final Collection<Region> regions, final Player access) {
        final StringBuilder sb = new StringBuilder();
        for (final Region region : regions) {
            if (sb.length() > 0) sb.append(Main.courier.format("region.+delimiter"));
            sb.append(Main.courier.format("region.+name", RegionExecutor.formatName(region), region.hasAccess(access)?1:0));
        }
        return sb.toString();
    }



    // ---- Working Region Management ----

    /** CommandSender ("SimpleClassName.Name") to working Region reference for commands */
    protected static final Map<String, Region> working = new HashMap<String, Region>();

    protected static Region putWorkingRegion(final CommandSender sender, final Region region) {
        return RegionExecutor.working.put(sender.getClass().getSimpleName() + "." + sender.getName(), region);
    }

    protected static Region removeWorkingRegion(final CommandSender sender) {
        return RegionExecutor.working.remove(sender.getClass().getSimpleName() + "." + sender.getName());
    }

    protected static Region getWorkingRegion(final CommandSender sender) {
        return RegionExecutor.working.get(sender.getClass().getSimpleName() + "." + sender.getName());
    }

    protected static String parse(final List<String> args, final int index, final String argument, final CommandSender sender) {
        if (args.size() < (index + 1)) {
            Main.courier.send(sender, "requiresArgument", argument);
            return null;
        }

        return args.get(index);
    }

}
