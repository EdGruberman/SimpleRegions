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
            Main.courier.send(sender, "requiresPlayer", label);
            return true;
        }

        final Player player = (Player) sender;
        final Location target = player.getTargetBlock((HashSet<Byte>) null, 50).getLocation();
        if (target == null) {
            Main.courier.send(sender, "blockNotIdentified");
            return true;
        }

        final Set<Region> regions = this.catalog.getRegions(target);
        final String names = RegionExecutor.formatNames(regions, player);
        Main.courier.send(sender, "target", names, regions.size(), this.catalog.isAllowed(player, target)?1:0, target.getBlockX(), target.getBlockY(), target.getBlockZ());
        return true;
    }

}
