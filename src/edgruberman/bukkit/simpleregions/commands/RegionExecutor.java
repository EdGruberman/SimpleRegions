package edgruberman.bukkit.simpleregions.commands;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

abstract class RegionExecutor extends Executor {

    protected final Catalog catalog;
    protected final Integer index;
    protected final boolean requiresOwner;

    RegionExecutor(final Catalog catalog) {
        this(catalog, null, false);
    }

    RegionExecutor(final Catalog catalog, final Integer index, final boolean requiresOwner) {
        this.catalog = catalog;
        this.index = index;
        this.requiresOwner = requiresOwner;
    }

    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        Region region = null;
        if (this.index != null) {
            region = this.identifyRegion(sender, args);
            if (region == null) {
                Main.courier.send(sender, "regionNotFound");
                return false;
            }

            if (this.requiresOwner && !RegionExecutor.isOwner(sender, region)) {
                Main.courier.send(sender, "requiresOwner", region.owners.toString().replaceAll("^\\[|\\]$", ""));
                return true;
            }
        }

        return this.execute(sender, command, label, args, region);
    }

    abstract boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, Region region);

    private Region identifyRegion(final CommandSender sender, final List<String> args) {
        Region found;
        if (this.index == null || args.size() <= this.index) {
            // <Region> and <World> arguments not supplied

            // Prioritize previously set working region
            found = RegionExecutor.getWorkingRegion(sender);

            if (found == null && (sender instanceof Player)) {
                // Assume current region if player is in only one
                final Set<Region> regions = this.catalog.getRegions(((Player) sender).getLocation());
                if (regions.size() == 1) found = regions.iterator().next();
            }

        } else {
            // <Region> and <World> arguments supplied
            final String region = args.get(this.index);
            final String world = (args.size() <= this.index + 1 && sender instanceof Player ? ((Player) sender).getWorld().getName() : args.get(this.index + 1));
            found = this.catalog.getRegion(region, world);
        }

        return found;
    }

    protected static boolean isOwner(final CommandSender sender, final Region region) {
        if (sender.hasPermission("simpleregions.region.owner.override")) return true;

        if ((sender instanceof Player) && region.isOwner((Player) sender)) return true;

        return false;
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

    protected static String parse(final List<String> args, final int index, final String parameter, final CommandSender sender) {
        if (args.size() < (index + 1)) {
            Main.courier.send(sender, "requiresParameter", parameter);
            return null;
        }

        return args.get(index);
    }



    // ---- Region Descriptions ----

    protected static void describeRegion(final Region region, final CommandSender sender) {
        // # 2 = Region, 3 = World, 4 = Active, 5 = Owners, 6 = Access, 7 = North, 8 = East, 9 = South, 10 = West, 11 = Down, 12 = Up
        Main.courier.send(sender, (region.isDefault() ? "describeDefault" : "describe")
                , region.formatName()
                , region.formatWorld()
                , region.isActive()
                , region.owners.toString().replaceAll("^\\[|\\]$", "")
                , region.access.toString().replaceAll("^\\[|\\]$", "")
                , region.getN(), region.getE(), region.getS(), region.getW(), region.getD(), region.getU()
        );

        if (region.isDefault()) return;

        RegionExecutor.describeRegionArea(region, sender);
        RegionExecutor.describeRegionVolume(region, sender);
    }

    /**
     * Generates a textual representation of the volumetric size
     * Example: 100x * 50y * 128z = 640,000 blocks
     */
    protected static void describeRegionVolume(final Region region, final CommandSender sender) {
        Integer sizeX = null;
        if (region.getN() != null && region.getS() != null)
            sizeX = Math.abs(region.getS() - region.getN()) + 1;

        Integer sizeY = null;
        if (region.getU() != null && region.getD() != null)
            sizeY = Math.abs(region.getU() - region.getD()) + 1;

        Integer sizeZ = null;
        if (region.getE() != null && region.getW() != null)
            sizeZ = Math.abs(region.getW() - region.getE()) + 1;

        // 2 = X Length, 3 = Y Length, 4 = Z Length, 5 = Volume
        Main.courier.send(sender, "volume"
            , (sizeX == null ? "?" : sizeX), (sizeY == null ? "?" : sizeY), (sizeZ == null ? "?" : sizeZ)
            , (sizeX != null && sizeY != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeY * sizeZ) : "?")
        );
    }

    /**
     * Generates a textual representation of the two-dimensional area
     * across the x and z axes
     * Example: 100x * 128z = 12,800 square meters
     */
    protected static void describeRegionArea(final Region region, final CommandSender sender) {
        Integer sizeX = null;
        if (region.getN() != null && region.getS() != null)
            sizeX = Math.abs(region.getS() - region.getN()) + 1;

        Integer sizeZ = null;
        if (region.getE() != null && region.getW() != null)
            sizeZ = Math.abs(region.getW() - region.getE()) + 1;

        // 2 = X Length, 3 = Z Length, 4 = Area
        Main.courier.send(sender, "area"
                , (sizeX == null ? "?" : sizeX), (sizeZ == null ? "?" : sizeZ)
                , (sizeX != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeZ) : "?")
        );
    }



    // ---- Utility ----

    /**
     * Combine all the elements of a list together with a delimiter between each.
     *
     * @param list list of elements to join
     * @param delim delimiter to place between each element
     * @return string combined with all elements and delimiters
     */
    protected static String join(final List<String> list, final String delim) {
        if (list == null || list.isEmpty()) return "";

        final StringBuilder sb = new StringBuilder();
        for (final String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());

        return sb.toString();
    }

}
