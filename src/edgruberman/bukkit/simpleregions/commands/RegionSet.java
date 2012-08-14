package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionSet extends RegionExecutor {

    public RegionSet(final Catalog catalog) {
        super(catalog, 0, false);
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        RegionExecutor.putWorkingRegion(sender, region);
        Main.courier.send(sender, "set", RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
