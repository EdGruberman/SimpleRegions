package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionOptionAdd extends RegionExecutor {

    public RegionOptionAdd(final Catalog catalog) {
        super(catalog, 1, true);
    }

    // usage: /<command> <Option>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String key = RegionExecutor.parse(args, 0, "<Option>", sender);
        if (key == null) return false;

        key = key.toLowerCase();
        if (!this.catalog.options.containsKey(key)) {
            Main.courier.send(sender, "optionAddUnrecognized", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        if (!region.options.add(key)) {
            Main.courier.send(sender, "optionAddExisting", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        this.catalog.registerOptions(region);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "optionAddSuccess", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
