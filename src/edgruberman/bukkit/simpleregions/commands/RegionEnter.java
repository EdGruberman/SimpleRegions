package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionEnter extends RegionExecutor {

    public RegionEnter(final Catalog catalog) {
        super(catalog, null, true);
    }

    // usage: /<command>[ <Message>]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        if (args.size() >= 1) {
            Main.courier.send(sender, String.format("enterPrevious", region.enter.getFormat().replace("&", "&&")));
            region.enter.setFormat(RegionExecutor.join(args, " "));
            this.catalog.repository.saveRegion(region, false);
            Main.courier.send(sender, "enterSet");
        }

        Main.courier.send(sender, "enterExisting", region.enter.getFormat().replace("&", "&&"));
        Main.courier.send(sender, "enterMessage", region.enter.formatted);
        return true;
    }

}
