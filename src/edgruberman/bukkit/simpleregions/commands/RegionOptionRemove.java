package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.options.Option;

public class RegionOptionRemove extends RegionExecutor {

    public RegionOptionRemove(final Catalog catalog) {
        super(catalog, 1, true);
    }

    // usage: /<command> <Option>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String key = RegionExecutor.parse(args, 0, "<Option>", sender);
        if (key == null) return false;

        key = key.toLowerCase();
        if (!region.options.remove(key)) {
            Main.courier.send(sender, "optionRemoveMissing", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        this.catalog.repository.saveRegion(region, false);
        final Option option = this.catalog.options.get(key);
        if (option != null) option.deregister(region);
        Main.courier.send(sender, "optionRemoveSuccess", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
