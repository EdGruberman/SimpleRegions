package edgruberman.bukkit.simpleregions.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionTarget extends RegionExecutor {

    public RegionTarget(final Catalog catalog) {
        super(catalog);
    }

    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        if (!(sender instanceof Player)) {
            Main.messenger.tell(sender, "requiresPlayer");
            return true;
        }

        final Player player = (Player) sender;
        final Location target = player.getTargetBlock((HashSet<Byte>) null, 50).getLocation();
        final Set<Region> regions = this.catalog.getRegions(target);

        final String names = Main.formatNames(regions, player);
        Main.messenger.tell(sender, (this.catalog.isAllowed(player, player.getLocation()) ? "targetHasAccess" : "targetNoAccess"), names, regions.size(), target.getBlockX(), target.getBlockY(), target.getBlockZ());
        return true;
    }

}
