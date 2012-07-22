package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;

public class RegionCreate implements CommandExecutor {

    private final Catalog catalog;

    public RegionCreate(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command> <Region>[ <World>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String name = Utility.parse(args, 0, "<Region>", sender);
        if (name == null) return false;

        if (!(sender instanceof Player) && args.length < 2) {
            Messenger.tell(sender, "requiresParameter", "<World>");
            return false;
        }

        final String worldName = (args.length >= 2 ? args[1] : ((Player) sender).getWorld().getName());
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Messenger.tell(sender, "worldNotFound", worldName);
            return true;
        }

        final edgruberman.bukkit.simpleregions.Region region = new edgruberman.bukkit.simpleregions.Region(world, name);
        this.catalog.addRegion(region);
        this.catalog.repository.saveRegion(region, false);
        Messenger.tell(sender, "regionCreated", region.getDisplayName(), region.world.getName());
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.set " + region.getDisplayName() + " " + region.world.getName());
        return true;
    }

}
