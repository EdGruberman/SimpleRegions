package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionSet implements CommandExecutor {

    private final Catalog catalog;

    public RegionSet(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 1, this.catalog, sender);
        if (region == null) return false;

        this.catalog.setWorkingRegion(sender, region);
        Messenger.tell(sender, "set", region.getDisplayName(), region.world.getName());
        return true;
    }

}
