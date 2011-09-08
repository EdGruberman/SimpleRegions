package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionDeactivate extends Action {
    
    public static final String NAME = "deactivate";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("-active", "-activate"));
    
    RegionDeactivate(final Command owner) {
        super(owner, RegionDeactivate.NAME, Permission.REGION_ACTIVATE);
        this.aliases.addAll(RegionDeactivate.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        if (!region.setActive(false)) {
            Main.messageManager.respond(context.sender, "Unable to deactivate " + region.getDisplayName() + " region.", MessageLevel.WARNING, false);
            return;
        }
        
        Main.saveRegion(region, false);
        
        Main.messageManager.respond(context.sender, "Deactivated " + region.getDisplayName() + " region.", MessageLevel.STATUS, false);
    }
}