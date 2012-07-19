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

public class RegionEnter implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionEnter(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    // usage: /<command>[ <Message>]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player) && args.length < 1) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<Message>"), MessageLevel.SEVERE, false);
            return false;
        }

        final Region region = this.catalog.getWorkingRegion(sender);
        if (region == null) {
            MessageManager.of(this.plugin).tell(sender, "Working §eregion not set§r; To set: /region_set <Region>", MessageLevel.SEVERE, false);
            return true;
        }

        if (!Utility.canUseOwnerCommands(region, sender)) {
            MessageManager.of(this.plugin).tell(sender, "Command cancelled when §cnot region owner§r", MessageLevel.SEVERE, false);
            return true;
        }

        if (args.length >= 1) {
            MessageManager.of(this.plugin).tell(sender, String.format("Existing region enter format: %1$s", region.enter.getFormat().replace("&", "&&")), MessageLevel.CONFIG, false);
            region.enter.setFormat(Utility.join(args));
            this.catalog.repository.saveRegion(region, false);
            MessageManager.of(this.plugin).tell(sender, "Region §2enter format set", MessageLevel.STATUS, false);
        }

        MessageManager.of(this.plugin).tell(sender, String.format("Region enter format: %1$s", region.enter.getFormat().replace("&", "&&")), MessageLevel.CONFIG, false);
        MessageManager.of(this.plugin).tell(sender, String.format("Region enter message: %1$s", region.enter.formatted), MessageLevel.CONFIG, false);
        return true;
    }

}
