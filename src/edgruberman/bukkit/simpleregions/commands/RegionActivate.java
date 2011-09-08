package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionActivate extends Action {
    
    public static final String NAME = "activate";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+active", "+activate"));
    
    RegionActivate(final Command owner) {
        super(owner, RegionActivate.NAME, Permission.REGION_ACTIVATE);
        this.aliases.addAll(RegionActivate.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        if (!region.setActive(true)) {
            Main.messageManager.respond(context.sender, "Unable to activate " + region.getDisplayName() + " region.", MessageLevel.WARNING, false);
            return;
        }
        
        // Unset region if matches
        boolean unset = false;
        if (region.equals(Region.working.get(context.sender))) {
            Region.working.remove(context.sender);
            unset = true;
        }
        
        Main.saveRegion(region, false);
        
        Main.messageManager.respond(context.sender, (unset ? "Unset and activated " : "Activated ") + region.getDisplayName() + " region.", MessageLevel.STATUS, false);
    }
}