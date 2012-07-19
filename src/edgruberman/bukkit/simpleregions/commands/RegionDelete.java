package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDelete implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionDelete(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    // usage: /<command> <Region> <World>
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<Region>"), MessageLevel.SEVERE, false);
            return false;
        }

        if (args.length < 2) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<World>"), MessageLevel.SEVERE, false);
            return false;
        }

        final Region region = Utility.parseRegion(this.plugin, this.catalog, sender, args, 0);
        if (region == null) return false;

        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.info " + region.getDisplayName() + " " + region.world.getName());
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.unset");
        this.catalog.removeRegion(region);
        this.catalog.repository.deleteRegion(region, false);
        MessageManager.of(this.plugin).tell(sender, String.format("§2Region deleted§r: %1$s in %2$s", region.getDisplayName(), region.world.getName()), MessageLevel.STATUS, false);
        return true;
    }

}
