package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionSize extends Action {

    public static final String NAME = "size";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("volume", "vol", "area", "blocks"));

    private final Region base;

    RegionSize(final Region owner) {
        super(owner, RegionSize.NAME, Permission.REGION_SIZE);
        this.base = owner;
        this.aliases.addAll(RegionSize.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null || region.isDefault()) {
            context.respond("Unable to determine region", MessageLevel.SEVERE);
            return;
        }

        context.respond("-- Region size for " + region.getDisplayName() + ":", MessageLevel.CONFIG);
        context.respond(region.describeArea(), MessageLevel.CONFIG);
        context.respond(region.describeVolume(), MessageLevel.CONFIG);
    }

}
