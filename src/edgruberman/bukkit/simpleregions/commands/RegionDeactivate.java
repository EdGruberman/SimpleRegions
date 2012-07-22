package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDeactivate implements CommandExecutor {

    private final Catalog catalog;

    public RegionDeactivate(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 0, this.catalog, sender);
        if (region == null) return false;

        if (!Utility.checkOwner(region, sender)) return true;

        region.setActive(false);
        Messenger.tell(sender, "deactivated", region.getDisplayName(), (region.world != null ? region.world.getName() : Region.SERVER_DEFAULT_DISPLAY));
        return true;
    }

}
