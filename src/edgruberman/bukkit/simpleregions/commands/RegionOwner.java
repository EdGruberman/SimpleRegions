package edgruberman.bukkit.simpleregions.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;
import edgruberman.bukkit.simpleregions.util.CaseInsensitiveString;

public class RegionOwner extends Action {

    public static final String NAME = "owner";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+owner", "-owner", "=owner", "+owners", "-owners", "=owners"));

    private final Region base;

    RegionOwner(final Region owner) {
        super(owner, RegionOwner.NAME, Permission.REGION_OWNER);
        this.base = owner;
        this.aliases.addAll(RegionOwner.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null || region.isDefault()) {
            context.respond("Unable to determine region.", MessageLevel.SEVERE);
            return;
        }

        final List<String> names = context.arguments.subList(context.actionIndex + 1, context.arguments.size());
        if (names.size() == 0) {
            context.respond("No names specified.", MessageLevel.WARNING);
            return;
        }

        final CaseInsensitiveString operation = new CaseInsensitiveString(context.arguments.get(context.actionIndex).substring(0, 1));
        if (operation.equals("+") || operation.equals("o")) {
            // Add owner
            this.edit(context, region, names, true);

        } else if (operation.equals("-")) {
            // Remove owner
            this.edit(context, region, names, false);

        } else if (operation.equals("=")) {
            // Set owners

            // Remove any existing entries not specified to be added
            final List<String> remove = new ArrayList<String>(region.owners);
            remove.removeAll(names);
            this.edit(context, region, remove, false);

            // Add new entries
            this.edit(context, region, names, true);
        }
    }

    private void edit(final Context context, final edgruberman.bukkit.simpleregions.Region region, final List<String> names, final boolean grant) {
        final String senderName = (context.player != null ? context.player.getDisplayName() : "CONSOLE");
        for (final String name : names) {
            if (!(grant ? region.owners.add(name) : region.owners.remove(name))) {
                context.respond("Region ownership " + (grant ? "already contains " : "does not contain ") + name + " in " + region.getDisplayName(), MessageLevel.WARNING);
                continue;
            }

            // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
            if (!grant && !context.sender.hasPermission(Permission.REGION_OWNER.toString()) && context.player != null && !region.isOwner(context.player)) {
                region.owners.add(name);
                context.respond("You can not remove your own ownership from " + region.getDisplayName(), MessageLevel.SEVERE);
                continue;
            }

            context.respond("Region ownership " + (grant ? "added to " : "removed from ") + name + " for " + region.getDisplayName(), MessageLevel.STATUS);
            if (region.isActive())
                this.base.sendIfOnline(name, MessageLevel.EVENT, (grant ? "You have been granted region ownership to " : "Your region ownership has been revoked from ") + region.getDisplayName() + " by " + senderName);

            this.base.catalog.repository.saveRegion(region, false);
        }
    }
}