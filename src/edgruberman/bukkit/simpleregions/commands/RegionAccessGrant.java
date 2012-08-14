package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionAccessGrant extends RegionExecutor {

    public RegionAccessGrant(final Catalog catalog) {
        super(catalog, 1, true);
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String access = RegionExecutor.parse(args, 0, "<Access>", sender);
        if (access == null) return false;

        if (region.access.contains(access)) {
            Main.courier.send(sender, "accessGrantAlready", access, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        region.access.add(access);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "accessGrantSuccess", access, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        final Player added = Bukkit.getServer().getPlayerExact(access);
        if (region.active && added != null)
            Main.courier.send(added, "accessGrantNotify", sender.getName(), RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        return true;
    }

}
