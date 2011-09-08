package edgruberman.bukkit.simpleregions.commands;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Index;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

// Syntax: /region[ current][ <Player>]
public class RegionCurrent extends Action {
    
    public static final String NAME = "current";
    
    RegionCurrent(final Command owner) {
        super(owner, RegionCurrent.NAME, Permission.REGION_CURRENT);
    }
    
    @Override
    void execute(final Context context) {
        RegionCurrent.message(context, null);
    }
    
    static void message(final Context context, final Location location) {
        String name = RegionCurrent.parsePlayerName(context);
        if (name == null) {
            Main.messageManager.respond(context.sender, "Unable to determine target player name.", MessageLevel.SEVERE, false);
            return;
        }
        
        Location target = location;
        String world = null;
        
        // If a specific location is not specific, use target player's current location
        if (location == null) {
            Player player = Region.getExactPlayer(name);
            if (player == null) {
                Main.messageManager.respond(context.sender, "Unable to find target player \"" + name + "\"", MessageLevel.SEVERE, false);
                return;
            }
            
            target = player.getLocation();
            world = player.getWorld().getName();
        }
        
        // Get applicable regions
        Set<edgruberman.bukkit.simpleregions.Region> regions = Index.getRegions(target);
        
        // Compile region name list
        String names = "";
        for (edgruberman.bukkit.simpleregions.Region region : regions) {
            if (names.length() != 0) names += ", ";
            names += region.getDisplayName();
        }
        
        // Compile response message
        String message = "Current region" + (regions.size() > 1 ? "s" : "");
        if (!name.equals(context.player.getName())) message += " for " + (world != null ? "[" + world + "] " : "") + name;
        if (location != null) message += " at (x:" + target.getBlockX() + " y:" + target.getBlockY() + " z:" + target.getBlockZ() + ")";
        
        message += ": ";
        if (regions.size() == 0) {
            message += "(none)";
        } else {
            message += names;
        }
        
        // Determine response level based on target access
        MessageLevel level = MessageLevel.STATUS;
        if (!Main.isAllowed(name, target))
            level = MessageLevel.WARNING;
        
        Main.messageManager.respond(context.sender, message, level, false);
    }
    
    private static String parsePlayerName(final Context context) {
        if (context.arguments.size() <= (context.actionIndex + 1))
            return (context.player == null ? null : context.player.getName());
        
        return context.arguments.get(context.actionIndex + 1);
    }
}