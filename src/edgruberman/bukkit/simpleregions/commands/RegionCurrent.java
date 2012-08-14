package edgruberman.bukkit.simpleregions.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionCurrent extends RegionExecutor {

    public RegionCurrent(final Catalog catalog) {
        super(catalog);
    }

    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requiresPlayer", label);
            return true;
        }

        final Player player = (Player) sender;
        final Set<Region> regions = this.catalog.getRegions(player.getLocation());
        final String names = RegionExecutor.formatNames(regions, player);
        Main.courier.send(sender, "current", names, regions.size(), this.catalog.isAllowed(player, player.getLocation())?1:0);
        return true;
    }

}
