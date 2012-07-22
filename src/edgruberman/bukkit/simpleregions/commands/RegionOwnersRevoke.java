package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionOwnersRevoke implements CommandExecutor {

    private final Catalog catalog;

    public RegionOwnersRevoke(final Catalog catalog) {
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

        if (!region.isDirectOwner(owner)) {
            Messenger.tell(sender, "ownerRevokeMissing", owner, region.formatName(), region.formatWorld());
            return true;
        }

        final String name = owner.toLowerCase();
        for (final String o : region.owners)
            if (o.toLowerCase().equals(name)) {
                region.owners.remove(o);
                break;
            }

        // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
        if (!sender.hasPermission("simpleregions.region.owner.override") && sender instanceof Player && !region.isOwner((Player) sender)) {
            region.owners.add(owner);
            Messenger.tell(sender, "ownerRevokePrevent");
            return true;
        }

        this.catalog.repository.saveRegion(region, false);
        Messenger.tell(sender, "ownerRevokeSuccess", owner, region.formatName(), region.formatWorld());
        return true;
    }

}
