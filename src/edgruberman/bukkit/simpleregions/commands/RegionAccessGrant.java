package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Region;

public class RegionAccessGrant implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionAccessGrant(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<Access>"), MessageLevel.SEVERE, false);
            return false;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<Region>"), MessageLevel.SEVERE, false);
            return false;
        }

        if (!(sender instanceof Player) && args.length < 3) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<World>"), MessageLevel.SEVERE, false);
            return false;
        }

        final Region region = Utility.parseRegion(this.plugin, this.catalog, sender, args, 1);
        if (region == null) return false;

        if (!Utility.canUseOwnerCommands(region, sender)) {
            MessageManager.of(this.plugin).tell(sender, "Command cancelled when §cnot region owner§r", MessageLevel.SEVERE, false);
            return true;
        }

        final String access = args[0];
        if (region.hasDirectAccess(access)) {
            MessageManager.of(this.plugin).tell(sender, String.format("Region access §ealready contains§r %1$s", access), MessageLevel.WARNING, false);
            return true;
        }

        region.access.add(access);
        this.catalog.repository.saveRegion(region, false);
        MessageManager.of(this.plugin).tell(sender, String.format("Region §2access added§r to %1$s for %2$s in %3$s", access, region.getDisplayName(), region.world.getName()), MessageLevel.STATUS, false);

        final Player added = Bukkit.getServer().getPlayerExact(access);
        if (region.isActive() && added != null)
            MessageManager.of(this.plugin).tell(added, String.format("Region §2access granted§r to you by %1$s for %2$s in %3$s", sender.getName(), region.getDisplayName(), region.world.getName()), MessageLevel.STATUS, true);

        return true;
    }

}
