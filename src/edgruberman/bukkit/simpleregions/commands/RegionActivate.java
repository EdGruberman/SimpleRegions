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

public class RegionActivate implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionActivate(final Plugin plugin, final Catalog catalog) {
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

        final Region region = Utility.parseRegion(this.plugin, this.catalog, sender, args, 1);
        if (region == null) return false;

        if (!Utility.canUseOwnerCommands(region, sender)) {
            MessageManager.of(this.plugin).tell(sender, "Command cancelled when §cnot region owner§r", MessageLevel.SEVERE, false);
            return true;
        }

        region.setActive(true);
        MessageManager.of(this.plugin).tell(sender, String.format("§2Activated§r region %1$s in %2$s", region.getDisplayName(), region.world.getName()), MessageLevel.STATUS);
        return true;
    }

}
