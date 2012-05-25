package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;
import edgruberman.bukkit.simpleregions.util.CaseInsensitiveString;

public class RegionSet extends Action {

    public static final String NAME = "set";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+set", "unset", "-set", "working"));

    private final Region base;

    RegionSet(final Region owner) {
        super(owner, RegionSet.NAME, Permission.REGION_SET);
        this.base = owner;
        this.aliases.addAll(RegionSet.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null) {
            context.respond("Unable to determine region.", MessageLevel.SEVERE);
            return;
        }

        final CaseInsensitiveString operation = new CaseInsensitiveString(context.arguments.get(context.actionIndex).substring(0, 1));
        final boolean set = (operation.equals("+") || operation.equals("s") || operation.equals("w"));
        this.setWorkingRegion(context, region, set);
    }

    boolean setWorkingRegion(final Context context, final edgruberman.bukkit.simpleregions.Region region, final boolean set) {
        final String world = (region.world == null ? edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT : region.world.getName());

        // Set Operation
        if (set) {
            this.base.working.put(context.sender, region);
            context.respond("Working region set to: [" + world + "] " + region.getDisplayName(), MessageLevel.STATUS);
            return true;
        }

        // Unset Operation
        if (!this.base.working.containsKey(context.sender)) {
            context.respond("Working region not currently set.", MessageLevel.WARNING);
            return false;
        }

        this.base.working.remove(context.sender);
        context.respond("Working region unset from: [" + world + "] " + region.getDisplayName(), MessageLevel.STATUS);
        return true;
    }
}