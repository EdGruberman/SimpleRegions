package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;

public class RegionUnset implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionUnset(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        this.catalog.unsetWorkingRegion(sender);
        MessageManager.of(this.plugin).tell(sender, String.format("§2Unset working region"), MessageLevel.STATUS, false);
        return true;
    }

}
