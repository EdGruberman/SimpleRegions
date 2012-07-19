package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;

public class RegionCreate implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionCreate(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    // usage: /<command> <Region>[ <World>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<Region>"), MessageLevel.SEVERE, false);
            return false;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<World>"), MessageLevel.SEVERE, false);
            return false;
        }

        final String name = args[0];
        final String worldName = (args.length >= 2 ? args[1] : ((Player) sender).getWorld().getName());
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            MessageManager.of(this.plugin).tell(sender, "World §enot found§r", MessageLevel.SEVERE, false);
            return true;
        }

        final edgruberman.bukkit.simpleregions.Region region = new edgruberman.bukkit.simpleregions.Region(world, name);
        this.catalog.addRegion(region);
        this.catalog.repository.saveRegion(region, false);
        MessageManager.of(this.plugin).tell(sender, String.format("§2Region created:§r %1$s in %2$s", region.getDisplayName(), region.world.getName()), MessageLevel.STATUS, false);
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.set " + region.getDisplayName() + " " + region.world.getName());
        return true;
    }

}
