package edgruberman.bukkit.simpleregions.commands.design;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Rename extends RegionExecutor {

    public Rename(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <New>[ <Region>[ <World>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        final String newName = RegionExecutor.parse(args, 0, "<New>", sender);
        if (newName == null) return false;

        // deny if name conflict
        Region conflict = null;
        final String compare = newName.toLowerCase();
        for (final Region r : this.catalog.indices.get(region.world).regions)
            if (r.name.toLowerCase().equals(compare)) {
                conflict = r;
                break;
            }
        if (conflict != null) {
            Main.courier.send(sender, "rename-conflict", RegionExecutor.formatName(conflict), RegionExecutor.formatWorld(conflict));
            return true;
        }

        final Region renamed = new Region(region, newName);

        // deregister old
        this.catalog.indices.get(region.world).deregister(region);
        this.catalog.repository.deleteRegion(region, false);

        // register new
        this.catalog.indices.get(region.world).register(renamed);
        this.catalog.repository.saveRegion(renamed, false);

        final Region working = RegionExecutor.getWorkingRegion(sender);
        if (working != null && working.equals(region))
            Bukkit.getPluginCommand("simpleregions:context").execute(sender, "simplregions:context", args.toArray(new String[] { RegionExecutor.formatName(renamed), RegionExecutor.formatWorld(renamed) }));

        Main.courier.send(sender, "rename", RegionExecutor.formatName(renamed), RegionExecutor.formatWorld(renamed), RegionExecutor.formatName(region));
        return true;
    }

}
