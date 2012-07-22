package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDelete implements CommandExecutor {

    private final Catalog catalog;

    public RegionDelete(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command> <Region> <World>
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 0, this.catalog, sender);
        if (region == null) return false;

        if (!Utility.checkOwner(region, sender)) return true;

        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.info " + region.getDisplayName() + " " + region.world.getName());
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.unset");
        this.catalog.removeRegion(region);
        this.catalog.repository.deleteRegion(region, false);
        Messenger.tell(sender, "regionDeleted", region.getDisplayName(), region.world.getName());
        return true;
    }

}
