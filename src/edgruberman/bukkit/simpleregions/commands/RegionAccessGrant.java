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

        if (region.hasDirectAccess(access)) {
            Main.messenger.tell(sender, "accessGrantAlready", access, region.formatName(), region.formatWorld());
            return true;
        }

        region.access.add(access);
        this.catalog.repository.saveRegion(region, false);
        Main.messenger.tell(sender, "accessGrantSuccess", access, region.formatName(), region.formatWorld());

        final Player added = Bukkit.getServer().getPlayerExact(access);
        if (region.isActive() && added != null)
            Main.messenger.tell(added, "accessGrantNotify", sender.getName(), region.formatName(), region.formatWorld());

        return true;
    }

}
