package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionOwnersReset implements CommandExecutor {

    private final Catalog catalog;

    public RegionOwnersReset(final Catalog catalog) {
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

        // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
        if (!sender.hasPermission("simpleregions.region.owner.override") && !sender.hasPermission(owner)) {
            Messenger.tell(sender, "ownerRevokePrevent");
            return true;
        }

        region.owners.clear();
        region.owners.add(owner);
        this.catalog.repository.saveRegion(region, false);
        Messenger.tell(sender, "ownerReset", owner, region.formatName(), region.formatWorld());
        return true;
    }

}
