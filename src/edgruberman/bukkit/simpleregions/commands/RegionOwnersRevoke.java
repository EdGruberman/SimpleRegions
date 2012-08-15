package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.Bukkit;
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

        if (!region.owners.contains(owner)) {
            Main.courier.send(sender, "ownerRevokeMissing", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        region.owners.remove(owner);

        // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
        if (!sender.hasPermission("simpleregions.override.commands") && sender instanceof Player && !region.owners.inherits(sender)) {
            region.owners.add(owner);
            Main.courier.send(sender, "ownerRevokePrevent");
            return true;
        }

        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "ownerRevokeSuccess", Bukkit.getOfflinePlayer(owner).getName(), RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
