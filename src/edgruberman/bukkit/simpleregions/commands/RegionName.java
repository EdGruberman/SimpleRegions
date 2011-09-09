package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionName extends Action {
    
    public static final String NAME = "name";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("=name"));
    
    RegionName(final Command owner) {
        super(owner, RegionName.NAME, Permission.REGION_NAME);
        this.aliases.addAll(RegionName.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null || region.isDefault()) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        String name = Command.join(context.arguments.subList(context.actionIndex + 1, context.arguments.size()), " ");
        if (name.length() == 0) {
            Main.messageManager.respond(context.sender, "No region name specified.", MessageLevel.WARNING, false);
            return;
        }
        
        if (!region.setName(name)) {
            Main.messageManager.respond(context.sender, "Unable to change region name for " + region.getDisplayName(), MessageLevel.SEVERE, false);
            return;
        }
        
        Main.saveRegion(region, false);
        Main.messageManager.respond(context.sender, "Region name changed to: " + region.getDisplayName(), MessageLevel.STATUS, false);
    }
}