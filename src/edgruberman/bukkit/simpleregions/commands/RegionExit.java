package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionExit extends RegionExecutor {

    public RegionExit(final Catalog catalog) {
        super(catalog, null, true);
    }

    // usage: /<command>[ <Message>]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        if (args.size() >= 1) {
            Main.messenger.tell(sender, String.format("exitPrevious", region.exit.getFormat().replace("&", "&&")));
            region.exit.setFormat(RegionExecutor.join(args, " "));
            this.catalog.repository.saveRegion(region, false);
            Main.messenger.tell(sender, "exitSet");
        }

        Main.messenger.tell(sender, String.format("exitExisting", region.exit.getFormat().replace("&", "&&")));
        Main.messenger.tell(sender, "exitMessage", region.exit.formatted);
        return true;
    }

}
