package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionAccessReset implements CommandExecutor {

    private final Catalog catalog;

    public RegionAccessReset(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 1, this.catalog, sender);
        if (region == null) return false;

        if (!Utility.checkOwner(region, sender)) return true;

        final String access = Utility.parse(args, 0, "<Access>", sender);
        if (access == null) return false;

        region.access.clear();
        region.access.add(access);
        this.catalog.repository.saveRegion(region, false);
        Messenger.tell(sender, "accessReset", access, region.formatName(), region.formatWorld());
        return true;
    }

}
