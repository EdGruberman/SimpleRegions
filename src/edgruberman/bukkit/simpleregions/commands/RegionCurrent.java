package edgruberman.bukkit.simpleregions.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionCurrent implements CommandExecutor {

    private final Catalog catalog;

    public RegionCurrent(final Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Messenger.tell(sender, "requiresPlayer");
            return true;
        }

        final Player player = (Player) sender;
        final Set<Region> regions = this.catalog.getRegions(player.getLocation());
        final String names = Main.formatNames(regions, player);
        Messenger.tell(sender, (this.catalog.isAllowed(player, player.getLocation()) ? "currentHasAccess" : "currentNoAccess"), names, regions.size());
        return true;
    }

}
