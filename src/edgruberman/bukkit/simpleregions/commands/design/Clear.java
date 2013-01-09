package edgruberman.bukkit.simpleregions.commands.design;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Clear extends RegionExecutor {

    public Clear(final Catalog catalog) {
        super(catalog);
    }

    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final Region cleared = RegionExecutor.removeWorkingRegion(sender);
        Main.courier.send(sender, "clear", ( cleared == null ? null : RegionExecutor.formatName(cleared) ), ( cleared == null ? null : RegionExecutor.formatWorld(cleared) ));
        return true;
    }

}
