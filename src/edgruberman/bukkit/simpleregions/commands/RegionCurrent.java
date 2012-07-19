package edgruberman.bukkit.simpleregions.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Region;

public class RegionCurrent implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionCurrent(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            MessageManager.of(this.plugin).tell(sender, "Command cancelled when §cnot in-game player", MessageLevel.SEVERE, false);
            return true;
        }

        final Player player = (Player) sender;
        final Set<Region> regions = this.catalog.getRegions(player.getLocation());

        String names = "";
        for (final Region region : regions) {
            if (names.length() != 0) names += ", ";
            names += String.format((region.hasAccess(player) ? "§2%1$s§r" : "§e%1$s§r"), region.getDisplayName());
        }
        final String message = String.format("Current regions: %1$s §8(%2$d)", names, regions.size());

        MessageLevel level = MessageLevel.STATUS;
        if (!this.catalog.isAllowed(player, player.getLocation()))
            level = MessageLevel.WARNING;

        MessageManager.of(this.plugin).tell(sender, message, level, false);
        return true;
    }

}
