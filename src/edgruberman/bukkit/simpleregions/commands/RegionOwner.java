package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionOwner extends Action {
    
    public static final String NAME = "owner";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+owner", "-owner", "=owner", "+owners", "-owners", "=owners"));
    
    RegionOwner(final Command owner) {
        super(owner, RegionOwner.NAME, Permission.REGION_ACTIVE);
        this.aliases.addAll(RegionOwner.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        List<String> names = context.arguments.subList(context.actionIndex + 1, context.arguments.size());
        if (names.size() == 0) {
            Main.messageManager.respond(context.sender, "No names specified.", MessageLevel.WARNING, false);
            return;
        }
        
        String operation = context.arguments.get(context.actionIndex).substring(0, 1);
        if (operation.equals("+") || operation.equals("o")) { 
            // Add owner
            RegionOwner.edit(context, region, names, true);
            
        } else if (operation.equals("-")) {
            // Remove owner
            RegionOwner.edit(context, region, names, false);
            
        } else if (operation.equals("=")) {
            // Set owners
            
            // Remove any existing entries not specified to be added
            List<String> remove = region.access.formatOwners();
            remove.removeAll(names);
            RegionOwner.edit(context, region, remove, false);
            
            // Add new entries
            RegionOwner.edit(context, region, names, true);
        }
    }
    
    private static void edit(final Context context, final edgruberman.bukkit.simpleregions.Region region, List<String> names, final boolean grant) {
        String senderName = (context.player != null ? context.player.getDisplayName() : "CONSOLE");
        for (String name : names) {
            if (!(grant ? region.access.addOwner(name) : region.access.removeOwner(name))) {
                Main.messageManager.respond(context.sender, "Region ownership " + (grant ? "already contains " : "does not contain ") + name + " in " + region.getDisplayName(), MessageLevel.WARNING, false);
                continue;
            }
            
            // Do not allow an owner to remove their own ownership accidentally if they can't add themselves back forcibly
            if (!grant && !context.sender.hasPermission(Permission.REGION_OWNER.toString()) && context.player != null && !region.access.isOwner(context.player)) {
                region.access.addOwner(name);
                Main.messageManager.respond(context.sender, "You can not remove your own ownership from " + region.getDisplayName(), MessageLevel.SEVERE, false);
                continue;
            }
            
            Main.messageManager.respond(context.sender, "Region ownership " + (grant ? "added to " : "removed from ") + name + " for " + region.getDisplayName(), MessageLevel.STATUS, false);
            if (region.isActive())
                Region.sendIfOnline(name, MessageLevel.EVENT, (grant ? "You have been granted region ownership to " : "Your region ownership has been revoked from ") + region.getDisplayName() + " by " + senderName);

            Main.saveRegion(region, false);
        }
    }
}