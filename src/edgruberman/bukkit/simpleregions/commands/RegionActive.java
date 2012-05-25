package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;
import edgruberman.bukkit.simpleregions.util.CaseInsensitiveString;

public class RegionActive extends Action {

    public static final String NAME = "active";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("activate", "+active", "+activate", "deactivate", "-active", "-activate"));

    private final Region base;
    private final RegionSet regionSet;

    RegionActive(final Region owner, final RegionSet regionSet) {
        super(owner, RegionActive.NAME, Permission.REGION_ACTIVE);
        this.base = owner;
        this.regionSet = regionSet;
        this.aliases.addAll(RegionActive.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null) {
            context.respond("Unable to determine region", MessageLevel.SEVERE);
            return;
        }

        final CaseInsensitiveString operation = new CaseInsensitiveString(context.arguments.get(context.actionIndex).substring(0, 1));
        final boolean active = (operation.equals("+") || operation.equals("a")); // false for "-" or "d"
        if (!region.setActive(active)) {
           context.respond("Unable to " + (active ? "activate" : "deactivate") + " region: " + region.getDisplayName(), MessageLevel.WARNING);
            return;
        }

       this.base.catalog.repository.saveRegion(region, false);
       context.respond((active ? "Activated " : "Deactivated ") + region.getDisplayName() + " region", MessageLevel.STATUS);

        // When deactivating, set working region if one not already set
        if (!active) {
            if (!this.base.working.containsKey(context.sender))
                this.regionSet.setWorkingRegion(context, region, true);

            return;
        }

        // When activating, unset working region if currently matches
        if (region.equals(this.base.working.get(context.sender)))
            this.regionSet.setWorkingRegion(context, region, false);

    }

}