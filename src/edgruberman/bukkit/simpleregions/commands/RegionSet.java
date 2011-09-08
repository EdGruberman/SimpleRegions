package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionSet extends Action {
    
    public static final String NAME = "set";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+set"));
    
    RegionSet(final Command owner) {
        super(owner, RegionSet.NAME, Permission.REGION_SET);
        this.aliases.addAll(RegionSet.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        Region.working.put(context.sender, region);
        
        String world = (region.getWorld() == null ? edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT : region.getWorld().getName());
        Main.messageManager.respond(context.sender, "Working region set to: [" + world + "] " + region.getDisplayName(), MessageLevel.STATUS, false);
    }
}