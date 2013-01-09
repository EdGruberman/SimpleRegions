package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Revoke extends OwnerExecutor {

    public Revoke(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String access = RegionExecutor.parse(args, 0, "<Access>", sender);
        if (access == null) return false;

        if (!region.access.contains(access)) {
            Main.courier.send(sender, "revoke-missing", access, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        region.access.remove(access);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "revoke", Bukkit.getOfflinePlayer(access).getName(), RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
