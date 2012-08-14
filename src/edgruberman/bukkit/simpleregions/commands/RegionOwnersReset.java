package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionOwnersReset extends RegionExecutor {

    public RegionOwnersReset(final Catalog catalog) {
        super(catalog, 1, true);
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String owner = RegionExecutor.parse(args, 0, "<Owner>", sender);
        if (owner == null) return false;

        // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
        if (!sender.hasPermission("simpleregions.region.owner.override") && !sender.hasPermission(owner)) {
            Main.courier.send(sender, "ownerRevokePrevent");
            return true;
        }

        region.owners.clear();
        region.owners.add(owner);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "ownerReset", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
