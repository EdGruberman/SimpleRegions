package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionCreate extends Action {

    public static final String NAME = "create";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("new", "add"));

    private final Region base;
    private final RegionSet regionSet;

    RegionCreate(final Region owner, final RegionSet regionSet) {
        super(owner, RegionCreate.NAME, Permission.REGION_CREATE);
        this.base = owner;
        this.regionSet = regionSet;
        this.aliases.addAll(RegionCreate.ALIASES);
    }

    @Override
    void execute(final Context context) {
        String name = null;
        if (context.arguments.size() > 1) name = context.arguments.get(context.arguments.size() - 1);
        if (name == null) {
            context.respond("No region name specified.", MessageLevel.WARNING);
            return;
        }

        final World world = RegionCreate.parseWorld(context);
        if (world == null) {
            context.respond("World unable to be determined.", MessageLevel.WARNING);
            return;
        }

        final edgruberman.bukkit.simpleregions.Region region = new edgruberman.bukkit.simpleregions.Region(this.base.accountManager, world, name);
        this.base.catalog.addRegion(region);
        this.base.catalog.repository.saveRegion(region, false);
        this.regionSet.setWorkingRegion(context, region, true);
        context.respond("Region created: " + region.getDisplayName(), MessageLevel.STATUS);
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
