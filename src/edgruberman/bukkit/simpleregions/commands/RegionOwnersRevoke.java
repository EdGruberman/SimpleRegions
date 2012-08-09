package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionOwnersRevoke extends RegionExecutor {

    public RegionOwnersRevoke(final Catalog catalog) {
        super(catalog, 1, true);
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String owner = RegionExecutor.parse(args, 0, "<Owner>", sender);
        if (owner == null) return false;

        if (!region.isDirectOwner(owner)) {
            Main.courier.send(sender, "ownerRevokeMissing", owner, region.formatName(), region.formatWorld());
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
            Main.courier.send(sender, "ownerRevokePrevent");
            return true;
        }

        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "ownerRevokeSuccess", owner, region.formatName(), region.formatWorld());
        return true;
    }

}
