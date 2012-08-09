package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDeactivate extends RegionExecutor {

    public RegionDeactivate(final Catalog catalog) {
        super(catalog, 0, true);
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        region.setActive(false);
        Main.courier.send(sender, "deactivated", region.formatName(), region.formatWorld());
        return true;
    }

}
