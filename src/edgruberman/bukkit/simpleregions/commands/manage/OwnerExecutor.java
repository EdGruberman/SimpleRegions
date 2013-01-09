package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public abstract class OwnerExecutor extends RegionExecutor {

    /** @param index -1 to force working region; null to not parse region; 0+ to parse region in [Region[ World]] format starting at index */
    protected OwnerExecutor(final Catalog catalog, final Integer index) {
        super(catalog, index);
    }

    protected abstract boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, Region region);

    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        if (!OwnerExecutor.isOwner(sender, region)) {
            Main.courier.send(sender, "requires-owner", label);
            return true;
        }

        return this.perform(sender, command, label, args, region);
    }

    public static boolean isOwner(final CommandSender sender, final Region region) {
        if (sender.hasPermission("simpleregions.override.commands")) return true;
        if ((sender instanceof Player) && region.owners.inherits(sender)) return true;
        return false;
    }

}
