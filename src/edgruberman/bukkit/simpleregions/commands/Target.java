package edgruberman.bukkit.simpleregions.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class Target implements CommandExecutor {

    private final Catalog catalog;

    public Target(final Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return true;
        }

        final Player player = (Player) sender;
        final Location target = player.getTargetBlock((HashSet<Byte>) null, 50).getLocation();
        if (target == null) {
            Main.courier.send(sender, "unknown-argument", "<Block>", target);
            return true;
        }

        final Set<Region> regions = this.catalog.cached(target);
        final String names = RegionExecutor.formatNames(regions, player);
        Main.courier.send(sender, "target", names, regions.size(), this.catalog.isAllowed(player, target)?1:0, target.getBlockX(), target.getBlockY(), target.getBlockZ());
        return true;
    }

}
