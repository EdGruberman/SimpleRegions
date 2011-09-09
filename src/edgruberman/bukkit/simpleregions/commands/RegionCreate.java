package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionCreate extends Action {
    
    public static final String NAME = "create";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("new", "add"));
    
    RegionCreate(final Command owner) {
        super(owner, RegionCreate.NAME, Permission.REGION_CREATE);
        this.aliases.addAll(RegionCreate.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        String name = null;
        if (context.arguments.size() > 1) name = context.arguments.get(context.arguments.size() - 1);
        if (name == null) {
            Main.messageManager.respond(context.sender, "No region name specified.", MessageLevel.WARNING, false);
            return;
        }
        
        World world = RegionCreate.parseWorld(context);
        if (world == null) {
            Main.messageManager.respond(context.sender, "World unable to be determined.", MessageLevel.WARNING, false);
            return;
        }
        
        edgruberman.bukkit.simpleregions.Region region = new edgruberman.bukkit.simpleregions.Region(world, name);
        RegionSet.setWorkingRegion(context.sender, region, true);
        Main.saveRegion(region, false);
        Main.messageManager.respond(context.sender, "Region created: " + region.getDisplayName(), MessageLevel.STATUS, false);
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