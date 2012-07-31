package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDelete extends RegionExecutor {

    public RegionDelete(final Catalog catalog) {
        super(catalog, 0, true);
    }

    // usage: /<command> <Region> <World>
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.info " + region.formatName() + " " + region.formatWorld());
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.unset");
        this.catalog.removeRegion(region);
        this.catalog.repository.deleteRegion(region, false);
        Main.messenger.tell(sender, "regionDeleted", region.formatName(), region.formatWorld());
        return true;
    }

}
