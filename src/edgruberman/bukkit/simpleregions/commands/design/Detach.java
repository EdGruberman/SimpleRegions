package edgruberman.bukkit.simpleregions.commands.design;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Option;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;
import edgruberman.bukkit.simpleregions.commands.manage.OwnerExecutor;

public class Detach extends OwnerExecutor {

    public Detach(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Option>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String key = RegionExecutor.parse(args, 0, "<Option>", sender);
        if (key == null) return false;

        key = key.toLowerCase();
        if (!region.options.remove(key)) {
            Main.courier.send(sender, "detach-missing", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        this.catalog.repository.saveRegion(region, false);
        final Option option = this.catalog.options.get(key);
        if (option != null) option.deregister(region);
        Main.courier.send(sender, "detach", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
