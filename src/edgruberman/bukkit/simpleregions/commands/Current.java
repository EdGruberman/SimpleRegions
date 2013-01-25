package edgruberman.bukkit.simpleregions.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class Current implements CommandExecutor {

    private final Catalog catalog;

    public Current(final Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return true;
        }

        final Player player = (Player) sender;
        final Set<Region> regions = this.catalog.cached(player.getLocation());
        final String names = RegionExecutor.formatNames(regions, player);
        Main.courier.send(sender, "current", names, regions.size(), this.catalog.isAllowed(player, player.getLocation())?1:0);
        return true;
    }

}
