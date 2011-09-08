package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionAccess extends Action {
    
    public static final String NAME = "access";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+", "-", "=", "+access", "-access", "=access"));
    
    RegionAccess(final Command owner) {
        super(owner, RegionAccess.NAME, Permission.REGION_ACTIVATE);
        this.aliases.addAll(RegionAccess.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        if (context.actionIndex == context.arguments.size() - 1) {
            Main.messageManager.respond(context.sender, "No names specified.", MessageLevel.WARNING, false);
            return;
        }
        
        String senderName = (context.player != null ? context.player.getDisplayName() : "CONSOLE");
        String operation = context.arguments.get(context.actionIndex).substring(0, 1);
        if (operation.equals("+") || operation.equals("a")) { 
            // Add access
            RegionAccess.edit(context, region, true);
            
        } else if (operation.equals("-")) {
            // Remove access
            RegionAccess.edit(context, region, false);
            
        } else if (operation.endsWith("=")) {
            // Set access

            // Remove any existing entries not specified to be added
            for (String name : region.access.formatAllowed()) {
                if (context.arguments.subList(context.actionIndex + 1, context.arguments.size()).contains(name)) continue; 
                
                region.access.revoke(name);
                Main.messageManager.respond(context.sender, "Access removed from " + name, MessageLevel.STATUS, false);
                if (region.isActive())
                    Region.sendIfOnline(name, MessageLevel.EVENT, "Your access has been removed from " + region.getDisplayName() + " region by " + senderName);
                
                Main.saveRegion(region, false);
            }
            
            // Add new entries
            RegionAccess.edit(context, region, true);
        }
    }
    
    private static void edit(final Context context, final edgruberman.bukkit.simpleregions.Region region, final boolean grant) {
        String senderName = (context.player != null ? context.player.getDisplayName() : "CONSOLE");
        for (String name : context.arguments.subList(context.actionIndex + 1, context.arguments.size())) {
            if ((grant ? region.access.grant(name) : region.access.revoke(name))) {
                Main.messageManager.respond(context.sender, "Access " + (grant ? "added to " : "removed from ") + name, MessageLevel.STATUS, false);
                if (region.isActive()) {
                    Region.sendIfOnline(name, MessageLevel.EVENT, (grant ? "You have been granted access to " : "Your access has been revoked from ") + region.getDisplayName() + " region by " + senderName);
                    Main.saveRegion(region, false);
                }
            } else {
                Main.messageManager.respond(context.sender, "Access " + (grant ? "already contains " : "does not contain ") + name, MessageLevel.WARNING, false);
            }
        }
    }
}