package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionAccessRevoke extends RegionExecutor {

    public RegionAccessRevoke(final Catalog catalog) {
        super(catalog, 1, true);
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String access = RegionExecutor.parse(args, 0, "<Access>", sender);
        if (access == null) return false;

        if (!region.hasDirectAccess(access)) {
            Main.courier.send(sender, "accessRevokeMissing", access, region.formatName(), region.formatWorld());
            return true;
        }

        for (final String o : region.access)
            if (o.equalsIgnoreCase(access)) {
                region.access.remove(access);
                break;
            }

        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "accessRevokeSuccess", access, region.formatName(), region.formatWorld());
        return true;
    }

}
