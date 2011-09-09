package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Index;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionDelete extends Action {
    
    public static final String NAME = "delete";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("del", "remove"));
    
    RegionDelete(final Command owner) {
        super(owner, RegionDelete.NAME, Permission.REGION_DELETE);
        this.aliases.addAll(RegionDelete.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null || region.isDefault()) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        boolean confirmed = false;
        if ((context.arguments.size() >= 2) && (context.arguments.get(context.arguments.size() - 1).equalsIgnoreCase("yes"))) confirmed = true;
        if (!confirmed) {
            Main.messageManager.respond(context.sender
                , "Are you sure you wish to delete the " + region.getDisplayName() + " region?\n"
                    + "To confirm: /" + context.owner.name + " " + region.getDisplayName() + " " + RegionDelete.NAME + " yes"
                , MessageLevel.WARNING
                , false
            );
            return;
        }   
        
        // Unset region if set
        edgruberman.bukkit.simpleregions.Region working = Region.working.get(context.sender);
        if (region.equals(working)) RegionSet.setWorkingRegion(context.sender, region, false);
        
        Index.remove(region);
        Main.deleteRegion(region, false);
        Main.messageManager.respond(context.sender, "Region deleted: " + region.getDisplayName(), MessageLevel.STATUS, false);
    }
    
    // Command Syntax: /region create[ <World>] <Region>
    static World parseWorld(final Context context) {
        // Assume player's world if not specified.
        if (context.arguments.size() <= 2) {
            if (context.player == null) return null;
            
            return context.player.getWorld();
        }
        
        String name = context.arguments.get(context.actionIndex + 1);
        if (name.equals(edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT)) return null;
        
        return context.owner.plugin.getServer().getWorld(name);
    }
}