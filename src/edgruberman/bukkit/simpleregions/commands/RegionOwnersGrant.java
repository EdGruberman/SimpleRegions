package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionOwnersGrant implements CommandExecutor {

    private final Catalog catalog;

    public RegionOwnersGrant(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 1, this.catalog, sender);
        if (region == null) return false;

        if (!Utility.checkOwner(region, sender)) return true;

        final String owner = Utility.parse(args, 0, "<Owner>", sender);
        if (owner == null) return false;

        if (region.isDirectOwner(owner)) {
            Messenger.tell(sender, "ownerGrantAlready", owner, region.getDisplayName(), region.world.getName());
            return true;
        }

        region.owners.add(owner);
        this.catalog.repository.saveRegion(region, false);
        Messenger.tell(sender, "ownerGrantSuccess", owner, region.getDisplayName(), (region.world != null ? region.world.getName() : Region.SERVER_DEFAULT_DISPLAY));

        final Player added = Bukkit.getServer().getPlayerExact(owner);
        if (region.isActive() && added != null)
            Messenger.tell(added, String.format("ownerGrantNotify", sender.getName(), region.getDisplayName(), (region.world != null ? region.world.getName() : Region.SERVER_DEFAULT_DISPLAY)));

        return true;
    }

}
