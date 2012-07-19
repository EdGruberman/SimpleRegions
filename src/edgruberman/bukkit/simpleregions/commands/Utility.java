package edgruberman.bukkit.simpleregions.commands;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Region;

public class Utility {

    static void describeRegion(final Plugin plugin, final Region region, final CommandSender sender) {
        MessageManager.of(plugin).tell(sender, String.format("-- Region: %1$s in %2$s"
                , region.getDisplayName()
                , (region.world == null ? Region.SERVER_DEFAULT_DISPLAY : region.world.getName()))
                , MessageLevel.CONFIG, false);
        MessageManager.of(plugin).tell(sender, String.format("Active: %1$s", region.isActive()), MessageLevel.CONFIG, false);
        if (!region.isDefault()) MessageManager.of(plugin).tell(sender, String.format("Owners: %1$s", region.owners.toString().replaceAll("^\\[|\\]$", "")), MessageLevel.CONFIG, false);
        MessageManager.of(plugin).tell(sender, String.format("Access: %1$s", region.access.toString().replaceAll("^\\[|\\]$", "")), MessageLevel.CONFIG, false);

        if (!region.isDefault()) {
            MessageManager.of(plugin).tell(sender, String.format("x:  [West] %1$d  to  %2$d [East]", region.getW(), region.getE()), MessageLevel.CONFIG, false);
            MessageManager.of(plugin).tell(sender, String.format("y:  [North] %1$d  to  %2$d [South]", region.getN(), region.getS()), MessageLevel.CONFIG, false);
            MessageManager.of(plugin).tell(sender, String.format("z:  [Down] %1$d  to  %2$d [Up]", region.getD(), region.getU()), MessageLevel.CONFIG, false);
        }
    }

    /**
     * Generates a textual representation of the volumetric size
     * Example: 100x * 50y * 128z = 640,000 blocks
     */
    static String describeRegionVolume(final Region region, final String format) {
        Integer sizeX = null;
        if (region.getN() != null && region.getS() != null)
            sizeX = Math.abs(region.getS() - region.getN()) + 1;

        Integer sizeY = null;
        if (region.getU() != null && region.getD() != null)
            sizeY = Math.abs(region.getU() - region.getD()) + 1;

        Integer sizeZ = null;
        if (region.getE() != null && region.getW() != null)
            sizeZ = Math.abs(region.getW() - region.getE()) + 1;

        final String size = String.format(
            format
            , (sizeX == null ? "?" : sizeX), (sizeY == null ? "?" : sizeY), (sizeZ == null ? "?" : sizeZ)
            , (sizeX != null && sizeY != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeY * sizeZ) : "?")
        );

        return size;
    }

    /**
     * Generates a textual representation of the two-dimensional area
     * across the x and z axes
     * Example: 100x * 128z = 12,800 square meters
     */
    static String describeRegionArea(final Region region, final String format) {
        Integer sizeX = null;
        if (region.getN() != null && region.getS() != null)
            sizeX = Math.abs(region.getS() - region.getN()) + 1;

        Integer sizeZ = null;
        if (region.getE() != null && region.getW() != null)
            sizeZ = Math.abs(region.getW() - region.getE()) + 1;

        final String area = String.format(
                format
                , (sizeX == null ? "?" : sizeX), (sizeZ == null ? "?" : sizeZ)
                , (sizeX != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeZ) : "?")
        );

        return area;
    }

    static Region parseRegion(final Plugin plugin, final Catalog catalog, final CommandSender sender, final String[] args, final Integer index) {
        Region found;
        if (index <= -1 || args.length <= index) {
            found = catalog.getWorkingRegion(sender);
            if (found == null)
                MessageManager.of(plugin).tell(sender, "Region §enot identified§r", MessageLevel.WARNING, false);

        } else {
            final String region = args[index];
            final String world = (args.length <= index + 1 ? ((Player) sender).getWorld().getName() : args[index + 1]);
            found = catalog.getRegion(region, world);
            if (found == null)
                MessageManager.of(plugin).tell(sender, String.format("Region §o%1$s§r §enot found§r in world §o%2$s", region, world), MessageLevel.WARNING, false);
        }

        return found;
    }

    static boolean canUseOwnerCommands(final Region region, final CommandSender sender) {
        if (sender.hasPermission("simpleregions.region.owner.override")) return true;

        if (!(sender instanceof Player)) return false;

        return region.isOwner((Player) sender);
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
