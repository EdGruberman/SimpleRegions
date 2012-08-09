package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionInfo extends RegionExecutor {

    public RegionInfo(final Catalog catalog) {
        super(catalog, 0, false);
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        RegionExecutor.describeRegion(region, sender);

        if (!RegionExecutor.isOwner(sender, region)) return true;

        // Instruct owners on how to define/activate
        if (!region.isActive()) {
            if (!region.isDefault() && !region.isDefined() && sender.hasPermission("simpleregions.region.define")) {
                Main.courier.send(sender, "undefined");
                return true;
            }

            Main.courier.send(sender, "inactive");
        }
        return true;
    }

}
