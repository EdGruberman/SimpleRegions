package edgruberman.bukkit.simpleregions.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Index;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public final class Region extends Command implements CommandExecutor {
    
    public static final String NAME = "region";
    
    static Map<CommandSender, edgruberman.bukkit.simpleregions.Region> working = new HashMap<CommandSender, edgruberman.bukkit.simpleregions.Region>();
    
    public Region(final JavaPlugin plugin) {
        super(plugin, Region.NAME, Permission.REGION);
        this.setExecutorOf(this);
        
        this.registerAction(new RegionCurrent(this), true);
        this.registerAction(new RegionTarget(this));
        this.registerAction(new RegionSet(this));
        this.registerAction(new RegionUnset(this));
        this.registerAction(new RegionDetail(this));
        this.registerAction(new RegionSize(this));
        this.registerAction(new RegionReload(this));
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        Context context = super.parse(this, sender, command, label, args);
        
        if (!this.isAllowed(context.sender)) {
            Main.messageManager.respond(context.sender, "You are not allowed to use the " + context.label + " command.", MessageLevel.RIGHTS, false);
            return true;
        }
        
        if (context.action == null) {
            Main.messageManager.respond(context.sender, "Unrecognized action for the " + context.label + " command.", MessageLevel.WARNING, false);
            return true;
        }
        
        if (!context.action.isAllowed(context.sender)) {
            Main.messageManager.respond(context.sender, "You are not allowed to use the " + context.action.name + " action of the " + context.label + " command.", MessageLevel.RIGHTS, false);
            return true;
        }
        
        context.action.execute(context);
        
        return true;
    }
    
    // Command Syntax: /region[[ <World>] <Region>][ <Action>][ <Parameters>]
    static World parseWorld(final Context context) {
        // Assume player's world if not specified.
        if (context.actionIndex != 2) {
            if (context.player == null) return null;
            
            return context.player.getWorld();
        }
        
        return context.owner.plugin.getServer().getWorld(context.arguments.get(context.actionIndex - 2));
    }
    
    // Command Syntax: /region[[ <World>] <Region>][ <Action>][ <Parameters>]
    static edgruberman.bukkit.simpleregions.Region parseRegion(final Context context) {
        if (context.actionIndex <= 0) {
            // Use current working region if specified
            if (Region.working.containsKey(context.sender)) return Region.working.get(context.sender);
            
            // Assume player's current region if not specified and player is in only one
            if (context.player == null) return null;
            
            Set<edgruberman.bukkit.simpleregions.Region> regions = Index.getRegions(context.player.getLocation());
            if (regions.size() != 1) return null;
            
            return regions.iterator().next();
        }
        
        World world = Region.parseWorld(context);
        if (world == null) return null;
        
        String regionName = context.arguments.get(context.actionIndex - 1);
        return Index.worlds.get(world).getRegions().get(regionName);
    }
}