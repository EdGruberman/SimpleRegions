package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionInfo implements CommandExecutor {

    private final Catalog catalog;

    public RegionInfo(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 0, this.catalog, sender);
        if (region == null) return false;

        Utility.describeRegion(region, sender);

        if (!Utility.checkOwner(region, sender)) return true;

        // Instruct owners on how to define/activate
        if (!region.isActive()) {
            if (!region.isDefault() && !region.isDefined() && sender.hasPermission("simpleregions.region.define")) {
                Messenger.tell(sender, "undefined");
                return true;
            }

            Messenger.tell(sender, "inactive");
        }
        return true;
    }

}
