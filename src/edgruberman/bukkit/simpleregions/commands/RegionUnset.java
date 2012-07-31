package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionUnset extends RegionExecutor {

    public RegionUnset(final Catalog catalog) {
        super(catalog);
    }

    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        RegionExecutor.removeWorkingRegion(sender);
        Main.messenger.tell(sender, "unset");
        return true;
    }

}
