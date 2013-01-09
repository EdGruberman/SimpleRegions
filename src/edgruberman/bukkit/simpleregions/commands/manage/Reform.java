package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Reform extends OwnerExecutor {

    public Reform(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String owner = RegionExecutor.parse(args, 0, "<Owner>", sender);
        if (owner == null) return false;

        // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
        if (!sender.hasPermission("simpleregions.override.commands") && !sender.hasPermission(owner)) {
            Main.courier.send(sender, "demote-prevent");
            return true;
        }

        owner = Bukkit.getOfflinePlayer(owner).getName();
        region.owners.clear();
        region.owners.add(owner);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "reform", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
