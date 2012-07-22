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
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionTarget implements CommandExecutor {

    private final Catalog catalog;

    public RegionTarget(final Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Messenger.tell(sender, "requiresPlayer");
            return true;
        }

        final Player player = (Player) sender;
        final Location target = player.getTargetBlock((HashSet<Byte>) null, 50).getLocation();
        final Set<Region> regions = this.catalog.getRegions(target);

        final String names = Main.formatNames(regions, player);
        Messenger.tell(sender, (this.catalog.isAllowed(player, player.getLocation()) ? "targetHasAccess" : "targetNoAccess"), names, regions.size(), target.getBlockX(), target.getBlockY(), target.getBlockZ());
        return true;
    }

}
