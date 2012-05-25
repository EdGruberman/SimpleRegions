package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionDelete extends Action {

    public static final String NAME = "delete";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("del", "remove"));

    private final Region base;
    private final RegionSet regionSet;

    RegionDelete(final Region owner, final RegionSet regionSet) {
        super(owner, RegionDelete.NAME, Permission.REGION_DELETE);
        this.base = owner;
        this.regionSet = regionSet;
        this.aliases.addAll(RegionDelete.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null || region.isDefault()) {
            context.respond("Unable to determine region.", MessageLevel.SEVERE);
            return;
        }

        boolean confirmed = false;
        if ((context.arguments.size() >= 2) && (context.arguments.get(context.arguments.size() - 1).equalsIgnoreCase("yes"))) confirmed = true;
        if (!confirmed) {
            RegionDetail.describe(context, region);
            context.respond("Are you sure you wish to delete the " + region.getDisplayName() + " region?", MessageLevel.WARNING);
            context.respond("To confirm: /" + context.owner.name + " " + region.getDisplayName() + " " + RegionDelete.NAME + " yes", MessageLevel.WARNING);
            return;
        }

        // Unset region if set
        final edgruberman.bukkit.simpleregions.Region working = this.base.working.get(context.sender);
        if (region.equals(working)) this.regionSet.setWorkingRegion(context, region, false);

        this.base.catalog.removeRegion(region);
        this.base.catalog.repository.deleteRegion(region, false);
        context.respond("Region deleted: " + region.getDisplayName(), MessageLevel.STATUS);
    }

    // Command Syntax: /region create[ <World>] <Region>
    static World parseWorld(final Context context) {
        // Assume player's world if not specified.
        if (context.arguments.size() <= 2) {
            if (context.player == null) return null;

            return context.player.getWorld();
        }

        final String name = context.arguments.get(context.actionIndex + 1);
        if (name.equals(edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT)) return null;

        return context.owner.plugin.getServer().getWorld(name);
    }

}
