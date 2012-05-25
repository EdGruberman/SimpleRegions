package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;
import edgruberman.bukkit.simpleregions.util.CaseInsensitiveString;

public class RegionAccess extends Action {

    public static final String NAME = "access";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+", "-", "=", "+access", "-access", "=access"));

    private final Region base;

    RegionAccess(final Region owner) {
        super(owner, RegionAccess.NAME, Permission.REGION_ACCESS);
        this.base = owner;
        this.aliases.addAll(RegionAccess.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null) {
            context.respond("Unable to determine region.", MessageLevel.SEVERE);
            return;
        }

        final List<String> names = context.arguments.subList(context.actionIndex + 1, context.arguments.size());
        if (names.size() == 0) {
            context.respond("No names specified.", MessageLevel.WARNING);
            return;
        }

        final CaseInsensitiveString operation = new CaseInsensitiveString(context.arguments.get(context.actionIndex).substring(0, 1));
        if (operation.equals("+") || operation.equals("a")) {
            // Add access
            this.edit(context, region, names, true);

        } else if (operation.equals("-")) {
            // Remove access
            this.edit(context, region, names, false);

        } else if (operation.equals("=")) {
            // Set access

            // Remove any existing entries not specified to be added
            final List<String> remove = region.access.formatAllowed();
            remove.removeAll(names);
            this.edit(context, region, remove, false);

            // Add new entries
            this.edit(context, region, names, true);

            RegionDetail.describe(context, region);
        }
    }

    private void edit(final Context context, final edgruberman.bukkit.simpleregions.Region region, final List<String> names, final boolean grant) {
        final String senderName = (context.player != null ? context.player.getDisplayName() : "CONSOLE");
        for (final String name : names) {
            if (!(grant ? region.access.grant(name) : region.access.revoke(name))) {
                context.respond("Region access " + (grant ? "already contains " : "does not contain ") + name + " in " + region.getDisplayName(), MessageLevel.WARNING);
                continue;
            }

            context.respond("Region access " + (grant ? "granted to " : "revoked from ") + name + " for " + region.getDisplayName(), MessageLevel.STATUS);
            if (region.isActive())
                this.base.sendIfOnline(name, MessageLevel.EVENT, (grant ? "You have been granted region access to " : "Your region access has been revoked from ") + region.getDisplayName() + " by " + senderName);

            this.base.catalog.repository.saveRegion(region, false);
        }
    }
}