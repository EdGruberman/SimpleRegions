package edgruberman.bukkit.simpleregions.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionCreate extends RegionExecutor {

    public RegionCreate(final Catalog catalog) {
        super(catalog);
    }

    // usage: /<command> <Region>[ <World>]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String name = RegionExecutor.parse(args, 0, "<Region>", sender);
        if (name == null) return false;

        if (name.equalsIgnoreCase(RegionExecutor.NAME_DEFAULT)) name = null;

        if (!(sender instanceof Player) && args.size() < 2) {
            Main.courier.send(sender, "requiresArgument", "<World>");
            return false;
        }

        final String worldName = (args.size() >= 2 ? args.get(1) : ((Player) sender).getWorld().getName());
        final World world = (worldName.equalsIgnoreCase(RegionExecutor.SERVER_DEFAULT) ? null : Bukkit.getWorld(worldName));
        if (world == null && !worldName.equalsIgnoreCase(RegionExecutor.SERVER_DEFAULT)) {
            Main.courier.send(sender, "worldNotFound", worldName);
            return true;
        }

        // deny if name conflict
        Region conflict = null;
        final String compare = name.toLowerCase();
        for (final Region r : this.catalog.indices.get(world.getName()).regions)
            if (r.name.toLowerCase().equals(compare)) {
                conflict = r;
                break;
            }
        if (conflict != null) {
            Main.courier.send(sender, "renameConflict", RegionExecutor.formatName(conflict), RegionExecutor.formatWorld(conflict));
            return true;
        }

        final Region created = new Region(world.getName(), name, Collections.<String>emptyList(), Collections.<String>emptyList());
        this.catalog.indices.get(created.world).register(created);
        this.catalog.repository.saveRegion(created, false);
        Main.courier.send(sender, "regionCreated", RegionExecutor.formatName(created), RegionExecutor.formatWorld(created));
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.set " + RegionExecutor.formatName(created) + " " + RegionExecutor.formatWorld(created));
        return true;
    }

}
