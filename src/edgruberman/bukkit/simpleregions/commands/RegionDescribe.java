package edgruberman.bukkit.simpleregions.commands;

import java.text.NumberFormat;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDescribe extends RegionExecutor {

    public RegionDescribe(final Catalog catalog) {
        super(catalog, 0, false);
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final Integer sizeX = (region.getMaxX() == null || region.getMinX() == null ? null : Math.abs(region.getMaxX() - region.getMinX()) + 1);
        final Integer sizeY = (region.getMaxY() == null || region.getMinY() == null ? null : Math.abs(region.getMaxY() - region.getMinY()) + 1);
        final Integer sizeZ = (region.getMaxZ() == null || region.getMinZ() == null ? null : Math.abs(region.getMaxZ() - region.getMinZ()) + 1);
        final Long area = (sizeX == null || sizeZ == null ? null : (long) sizeX * sizeZ);
        final Long volume = (sizeX == null || sizeZ == null ? null : (long) sizeX * sizeY * sizeZ);

        // # 1 = Region, 2 = World, 3 = isActive(0|1), 4 = Owners, 5 = Access, 6 = Min X, 7 = Max X, 8 = Min Y, 9 = Max Y, 10 = Min Z, 11 = Max Z, 12 = X Length, 13 = Y Length, 14 = Z Length, 15 = Area, 16 = Volume, 17 = hasAccess(0|1), 18 = isOwner(0|1)
        Main.courier.send(sender, (region.isDefault() ? "describe.default" : "describe.normal")
                , RegionExecutor.formatName(region)
                , RegionExecutor.formatWorld(region)
                , region.active?1:0
                , region.owners.toString().replaceAll("^\\[|\\]$", "")
                , region.access.toString().replaceAll("^\\[|\\]$", "")
                , this.coalesce(region.getMinX(), region.getX1()), this.coalesce(region.getMaxX(), region.getX2())
                , this.coalesce(region.getMinY(), region.getY1()), this.coalesce(region.getMaxY(), region.getY2())
                , this.coalesce(region.getMinZ(), region.getZ1()), this.coalesce(region.getMaxZ(), region.getZ2())
                , (sizeX != null ? Integer.toString(sizeX) : "?")
                , (sizeY != null ? Integer.toString(sizeY) : "?")
                , (sizeZ != null ? Integer.toString(sizeZ) : "?")
                , (area != null ? NumberFormat.getInstance().format(area) : "?")
                , (volume != null ? NumberFormat.getInstance().format(volume) : "?")
                , region.hasAccess(sender)?1:0
                , region.owners.inherits(sender)?1:0
        );

        if (region.active || !RegionExecutor.isOwner(sender, region)) return true;

        // instruct owners on how to define
        if (!region.isDefault() && !region.isDefined() && sender.hasPermission("simpleregions.region.define")) {
            Main.courier.send(sender, "undefined");
            return true;
        }

        // instruct owners on how to activate
        Main.courier.send(sender, "inactive");
        return true;
    }

    private Integer coalesce(final Integer i1, final Integer i2) {
        return (i1 != null ? i1 : i2);
    }

}
