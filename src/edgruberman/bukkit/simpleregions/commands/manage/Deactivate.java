package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Deactivate extends OwnerExecutor {

    public Deactivate(final Catalog catalog) {
        super(catalog, 0);
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        region.active = false;
        if (!region.isDefault()) this.catalog.indices.get(region.world).refresh(region);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "deactivate", RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
        return true;
    }

}
