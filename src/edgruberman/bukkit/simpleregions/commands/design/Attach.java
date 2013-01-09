package edgruberman.bukkit.simpleregions.commands.design;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;
import edgruberman.bukkit.simpleregions.commands.manage.OwnerExecutor;

public class Attach extends OwnerExecutor {

    public Attach(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Option>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String key = RegionExecutor.parse(args, 0, "<Option>", sender);
        if (key == null) return false;

        key = key.toLowerCase();
        if (!this.catalog.options.containsKey(key)) {
            Main.courier.send(sender, "attach-unrecognized", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        if (!region.options.add(key)) {
            Main.courier.send(sender, "attach-existing", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        this.catalog.registerOptions(region);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "attach", key, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
