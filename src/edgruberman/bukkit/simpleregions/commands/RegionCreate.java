package edgruberman.bukkit.simpleregions.commands;

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
        final String name = RegionExecutor.parse(args, 0, "<Region>", sender);
        if (name == null) return false;

        if (!(sender instanceof Player) && args.size() < 2) {
            Main.courier.send(sender, "requiresParameter", "<World>");
            return false;
        }

        final String worldName = (args.size() >= 2 ? args.get(1) : ((Player) sender).getWorld().getName());
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Main.courier.send(sender, "worldNotFound", worldName);
            return true;
        }

        final edgruberman.bukkit.simpleregions.Region created = new edgruberman.bukkit.simpleregions.Region(world, name);
        this.catalog.addRegion(created);
        this.catalog.repository.saveRegion(created, false);
        Main.courier.send(sender, "regionCreated", created.formatName(), created.formatWorld());
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.set " + created.formatName() + " " + created.formatWorld());
        return true;
    }

}
