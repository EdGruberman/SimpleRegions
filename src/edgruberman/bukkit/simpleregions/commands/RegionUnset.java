package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;

public class RegionUnset implements CommandExecutor {

    private final Catalog catalog;

    public RegionUnset(final Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        this.catalog.unsetWorkingRegion(sender);
        Messenger.tell(sender, "unset");
        return true;
    }

}
