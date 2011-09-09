package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;
import edgruberman.java.CaseInsensitiveString;

public class RegionAccess extends Action {
    
    public static final String NAME = "access";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+", "-", "=", "+access", "-access", "=access"));
    
    RegionAccess(final Command owner) {
        super(owner, RegionAccess.NAME, Permission.REGION_ACCESS);
        this.aliases.addAll(RegionAccess.ALIASES);
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
        
        CaseInsensitiveString operation = new CaseInsensitiveString(context.arguments.get(context.actionIndex).substring(0, 1));
        if (operation.equals("+") || operation.equals("a")) { 
            // Add access
            RegionAccess.edit(context, region, names, true);
            
        } else if (operation.equals("-")) {
            // Remove access
            RegionAccess.edit(context, region, names, false);
            
        } else if (operation.equals("=")) {
            // Set access
            
            // Remove any existing entries not specified to be added
            List<String> remove = region.access.formatAllowed();
            remove.removeAll(names);
            RegionAccess.edit(context, region, remove, false);
            
            // Add new entries
            RegionAccess.edit(context, region, names, true);
            
            RegionDetail.describe(context, region);
        }
    }
    
    private static void edit(final Context context, final edgruberman.bukkit.simpleregions.Region region, List<String> names, final boolean grant) {
        String senderName = (context.player != null ? context.player.getDisplayName() : "CONSOLE");
        for (String name : names) {
            if (!(grant ? region.access.grant(name) : region.access.revoke(name))) {
                Main.messageManager.respond(context.sender, "Region access " + (grant ? "already contains " : "does not contain ") + name + " in " + region.getDisplayName(), MessageLevel.WARNING, false);
                continue;
            }
            
            Main.messageManager.respond(context.sender, "Region access " + (grant ? "granted to " : "revoked from ") + name + " for " + region.getDisplayName(), MessageLevel.STATUS, false);
            if (region.isActive())
                Region.sendIfOnline(name, MessageLevel.EVENT, (grant ? "You have been granted region access to " : "Your region access has been revoked from ") + region.getDisplayName() + " by " + senderName);
            
            Main.saveRegion(region, false);
        }
    }
}