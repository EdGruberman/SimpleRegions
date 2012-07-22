package edgruberman.bukkit.simpleregions.commands;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class Utility {

    static void describeRegion(final Region region, final CommandSender sender) {
        // # 2 = Region, 3 = World, 4 = Active, 5 = Owners, 6 = Access, 7 = North, 8 = East, 9 = South, 10 = West, 11 = Down, 12 = Up
        for (final String format : Messenger.get().formats.getStringList((region.isDefault() ? "describeDefault" : "describe")))
            Messenger.get().sendMessage(sender, format
                    , region.formatName()
                    , region.formatWorld()
                    , region.isActive()
                    , region.owners.toString().replaceAll("^\\[|\\]$", "")
                    , region.access.toString().replaceAll("^\\[|\\]$", "")
                    , region.getN(), region.getE(), region.getS(), region.getW(), region.getD(), region.getU()
            );

        if (region.isDefault()) return;

        Utility.describeRegionArea(region, sender);
        Utility.describeRegionVolume(region, sender);
    }

    /**
     * Generates a textual representation of the volumetric size
     * Example: 100x * 50y * 128z = 640,000 blocks
     */
    static void describeRegionVolume(final Region region, final CommandSender sender) {
        Integer sizeX = null;
        if (region.getN() != null && region.getS() != null)
            sizeX = Math.abs(region.getS() - region.getN()) + 1;

        Integer sizeY = null;
        if (region.getU() != null && region.getD() != null)
            sizeY = Math.abs(region.getU() - region.getD()) + 1;

        Integer sizeZ = null;
        if (region.getE() != null && region.getW() != null)
            sizeZ = Math.abs(region.getW() - region.getE()) + 1;

        Messenger.get().sendMessage(sender, Messenger.getFormat("volume")
            , (sizeX == null ? "?" : sizeX), (sizeY == null ? "?" : sizeY), (sizeZ == null ? "?" : sizeZ)
            , (sizeX != null && sizeY != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeY * sizeZ) : "?")
        );
    }

    /**
     * Generates a textual representation of the two-dimensional area
     * across the x and z axes
     * Example: 100x * 128z = 12,800 square meters
     */
    static void describeRegionArea(final Region region, final CommandSender sender) {
        Integer sizeX = null;
        if (region.getN() != null && region.getS() != null)
            sizeX = Math.abs(region.getS() - region.getN()) + 1;

        Integer sizeZ = null;
        if (region.getE() != null && region.getW() != null)
            sizeZ = Math.abs(region.getW() - region.getE()) + 1;

        Messenger.get().sendMessage(sender, Messenger.getFormat("area")
                , (sizeX == null ? "?" : sizeX), (sizeZ == null ? "?" : sizeZ)
                , (sizeX != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeZ) : "?")
        );
    }

    static Region parseRegion(final String[] args, final Integer index, final Catalog catalog, final CommandSender sender) {
        if (!(sender instanceof Player)) {
            if (args.length < (index + 1)) {
                Messenger.tell(sender, "requiresParameter", "<Region>");
                return null;
            }

            if (args.length < (index + 2)) {
                Messenger.tell(sender, "requiresParameter", "<World>");
                return null;
            }
        }

        Region found;
        if (args.length <= index) {
            found = catalog.getWorkingRegion(sender);
            if (found == null) Messenger.tell(sender, "regionNotIdentified");

        } else {
            final String region = args[index];
            final String world = (args.length <= index + 1 ? ((Player) sender).getWorld().getName() : args[index + 1]);
            found = catalog.getRegion(region, world);
            if (found == null) Messenger.tell(sender, "regionNotFound", region, world);
        }

        return found;
    }

    static boolean checkOwner(final Region region, final CommandSender sender) {
        if (sender.hasPermission("simpleregions.region.owner.override")) return true;

        if ((sender instanceof Player) && region.isOwner((Player) sender)) return true;

        Messenger.tell(sender, "requiresOwner", region.owners.toString().replaceAll("^\\[|\\]$", ""));
        return false;
    }

    static String parse(final String[] args, final int index, final String parameter, final CommandSender sender) {
        if (args.length < (index + 1)) {
            Messenger.tell(sender, "requiresParameter", parameter);
            return null;
        }

        return args[index];
    }

    /**
     * Concatenate all string elements of an array together with a space.
     *
     * @param s string array
     * @return concatenated elements
     */
    static String join(final String[] s) {
        return Utility.join(Arrays.asList(s), " ");
    }

    /**
     * Combine all the elements of a list together with a delimiter between each.
     *
     * @param list list of elements to join
     * @param delim delimiter to place between each element
     * @return string combined with all elements and delimiters
     */
    static String join(final List<String> list, final String delim) {
        if (list == null || list.isEmpty()) return "";

        final StringBuilder sb = new StringBuilder();
        for (final String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());

        return sb.toString();
    }

}
