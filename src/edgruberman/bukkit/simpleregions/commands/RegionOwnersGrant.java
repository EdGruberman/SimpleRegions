package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionOwnersGrant extends RegionExecutor {

    public RegionOwnersGrant(final Catalog catalog) {
        super(catalog, 1, true);
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String owner = RegionExecutor.parse(args, 0, "<Owner>", sender);
        if (owner == null) return false;

        if (region.owners.contains(owner)) {
            Main.courier.send(sender, "ownerGrantAlready", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        region.owners.add(owner);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "ownerGrantSuccess", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        final Player added = Bukkit.getServer().getPlayerExact(owner);
        if (region.active && added != null)
            Main.courier.send(added, "ownerGrantNotify", sender.getName(), RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        return true;
    }

}
