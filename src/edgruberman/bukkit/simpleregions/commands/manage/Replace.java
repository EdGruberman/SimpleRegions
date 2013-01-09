package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Replace extends OwnerExecutor {

    public Replace(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String access = RegionExecutor.parse(args, 0, "<Access>", sender);
        if (access == null) return false;

        access = Bukkit.getOfflinePlayer(access).getName();
        region.access.clear();
        region.access.add(access);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "replace", access, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
