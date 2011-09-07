package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionSize extends Action {
    
    public static final String NAME = "size";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("volume", "vol", "area", "blocks"));
    
    RegionSize(final Command owner) {
        super(owner, RegionSize.NAME, Permission.REGION_SIZE);
        this.aliases.addAll(RegionSize.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        if (region.isDefault()) {
            Main.messageManager.respond(context.sender, "Default region size will not be calculated.", MessageLevel.WARNING, false);
            return;
        }
        
        Main.messageManager.respond(context.sender, "-- Region Size for " + region.getName() + ":\n" + region.describeArea() + "\n" + region.describeVolume(), MessageLevel.STATUS, false);
    }
}