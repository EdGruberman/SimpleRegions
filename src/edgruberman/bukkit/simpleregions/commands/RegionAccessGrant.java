package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionAccessGrant implements CommandExecutor {

    private final Catalog catalog;

    public RegionAccessGrant(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 1, this.catalog, sender);
        if (region == null) return false;

        if (!Utility.checkOwner(region, sender)) return true;

        final String access = Utility.parse(args, 0, "<Access>", sender);
        if (access == null) return false;

        if (region.hasDirectAccess(access)) {
            Messenger.tell(sender, "accessGrantAlready", access, region.getDisplayName(), region.world.getName());
            return true;
        }

        region.access.add(access);
        this.catalog.repository.saveRegion(region, false);
        Messenger.tell(sender, "accessGrantSuccess", access, region.getDisplayName(), region.world.getName());

        final Player added = Bukkit.getServer().getPlayerExact(access);
        if (region.isActive() && added != null)
            Messenger.tell(added, "accessGrantNotify", sender.getName(), region.getDisplayName(), region.world.getName());

        return true;
    }

}
