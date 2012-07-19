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

public class RegionOwnersReset implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionOwnersReset(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            MessageManager.of(this.plugin).tell(sender, String.format("§cMissing §o%1$s§r parameter", "<Owner>"), MessageLevel.SEVERE, false);
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

        final String owner = args[0];

        // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
        if (!sender.hasPermission("simpleregions.region.owner.override") && !sender.getName().equalsIgnoreCase(owner)) {
            MessageManager.of(this.plugin).tell(sender, "You can §enot remove yourself§r as a region owner", MessageLevel.WARNING, false);
            return true;
        }

        region.owners.clear();
        region.owners.add(owner);
        this.catalog.repository.saveRegion(region, false);
        MessageManager.of(this.plugin).tell(sender, String.format("Region §2owners reset§r to %1$s for %2$s in %3$s", owner, region.getDisplayName(), region.world.getName()), MessageLevel.STATUS, false);
        return true;
    }

}
