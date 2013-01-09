package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Demote extends OwnerExecutor {

    public Demote(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String owner = RegionExecutor.parse(args, 0, "<Owner>", sender);
        if (owner == null) return false;

        if (!region.owners.contains(owner)) {
            Main.courier.send(sender, "demote-missing", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        region.owners.remove(owner);

        // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
        if (!sender.hasPermission("simpleregions.override.commands") && sender instanceof Player && !region.owners.inherits(sender)) {
            region.owners.add(owner);
            Main.courier.send(sender, "demote-prevent");
            return true;
        }

        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "demote", Bukkit.getOfflinePlayer(owner).getName(), RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
