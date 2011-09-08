package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionActive extends Action {
    
    public static final String NAME = "active";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("activate", "+active", "+activate", "deactivate", "-active", "-activate"));
    
    RegionActive(final Command owner) {
        super(owner, RegionActive.NAME, Permission.REGION_ACTIVE);
        this.aliases.addAll(RegionActive.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        String operation = context.arguments.get(context.actionIndex).substring(0, 1);
        boolean active = (operation.equals("+") || operation.equals("a")); // false for "-" or "d"
        if (!region.setActive(active)) {
            Main.messageManager.respond(context.sender, "Unable to " + (active ? "activate " : "deactivate ") + region.getDisplayName() + " region.", MessageLevel.WARNING, false);
            return;
        }
        
        Main.saveRegion(region, false);
        Main.messageManager.respond(context.sender, (active ? "Activated " : "Deactivated ") + region.getDisplayName() + " region.", MessageLevel.STATUS, false);
        
        // When deactivating, set working region if one not already set
        if (!active) {
            if (!Region.working.containsKey(context.sender))
                RegionSet.setWorkingRegion(context.sender, region, true);
                
            return;
        }
        
        // When activating, unset working region if currently matches
        if (region.equals(Region.working.get(context.sender)))
            RegionSet.setWorkingRegion(context.sender, region, false);
        
    }
}