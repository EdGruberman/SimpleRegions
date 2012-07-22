package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionEnter implements CommandExecutor {

    private final Catalog catalog;

    public RegionEnter(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command>[ <Message>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = this.catalog.getWorkingRegion(sender);
        if (region == null) {
            Messenger.tell(sender, "workingRegionNotSet");
            return true;
        }

        if (!Utility.checkOwner(region, sender)) return true;

        if (args.length >= 1) {
            Messenger.tell(sender, String.format("enterPrevious", region.enter.getFormat().replace("&", "&&")));
            region.enter.setFormat(Utility.join(args));
            this.catalog.repository.saveRegion(region, false);
            Messenger.tell(sender, "enterSet");
        }

        Messenger.tell(sender, "enterExisting", region.enter.getFormat().replace("&", "&&"));
        Messenger.tell(sender, "enterMessage", region.enter.formatted);
        return true;
    }

}
