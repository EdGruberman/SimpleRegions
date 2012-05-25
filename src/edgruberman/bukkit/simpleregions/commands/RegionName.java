package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionName extends Action {

    public static final String NAME = "name";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("=name"));

    private final Region base;

    RegionName(final Region owner) {
        super(owner, RegionName.NAME, Permission.REGION_NAME);
        this.base = owner;
        this.aliases.addAll(RegionName.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null || region.isDefault()) {
            context.respond("Unable to determine region.", MessageLevel.SEVERE);
            return;
        }

        final String name = Command.join(context.arguments.subList(context.actionIndex + 1, context.arguments.size()), " ");
        if (name.length() == 0) {
            context.respond("No region name specified.", MessageLevel.WARNING);
            return;
        }

        if (!region.setName(name)) {
            context.respond("Unable to change region name for " + region.getDisplayName(), MessageLevel.SEVERE);
            return;
        }

        this.base.catalog.repository.saveRegion(region, false);
        context.respond("Region name changed to: " + region.getDisplayName(), MessageLevel.STATUS);
    }
}