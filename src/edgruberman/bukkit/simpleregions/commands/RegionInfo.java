package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Region;

public class RegionInfo implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionInfo(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    // usage: /<command>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player) && args.length < 1) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<Region>"), MessageLevel.SEVERE, false);
            return false;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<World>"), MessageLevel.SEVERE, false);
            return false;
        }

        final Region region = Utility.parseRegion(this.plugin, this.catalog, sender, args, 0);
        if (region == null) return false;

        Utility.describeRegion(this.plugin, region, sender);
        if (!region.isDefault()) {
            MessageManager.of(this.plugin).tell(sender, Utility.describeRegionArea(region, "Area: %1$sx * %2$sz = %3$s blocks squared"), MessageLevel.CONFIG, false);
            MessageManager.of(this.plugin).tell(sender, Utility.describeRegionVolume(region, "Volume: %1$sx * %2$sy * %3$sz = %4$s blocks cubed"), MessageLevel.CONFIG, false);
        }

        if (!Utility.canUseOwnerCommands(region, sender)) return true;

        // Instruct owners on how to define/activate
        if (!region.isActive()) {
            if (!region.isDefault() && !region.isDefined() && sender.hasPermission("simpleregions.region.define")) {
                MessageManager.of(this.plugin).tell(sender, "Undefined region; To define: /define", MessageLevel.NOTICE, false);
                return true;
            }

            MessageManager.of(this.plugin).tell(sender, "Inactive region; To activate: /region_on", MessageLevel.NOTICE, false);
        }
        return true;
    }

}
