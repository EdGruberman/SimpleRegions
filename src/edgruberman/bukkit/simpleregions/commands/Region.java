package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Index;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public final class Region extends Command implements CommandExecutor {
    
    public static final String NAME = "region";
    public static final Set<String> OWNER_ALLOWED = new HashSet<String>(Arrays.asList(RegionActive.NAME, RegionAccess.NAME, RegionOwner.NAME));
    
    static Map<CommandSender, edgruberman.bukkit.simpleregions.Region> working = new HashMap<CommandSender, edgruberman.bukkit.simpleregions.Region>();
    
    public Region(final JavaPlugin plugin) {
        super(plugin, Region.NAME, Permission.REGION);
        this.setExecutorOf(this);
        
        this.registerAction(new RegionCurrent(this), true);
        this.registerAction(new RegionTarget(this));
        this.registerAction(new RegionSet(this));
        this.registerAction(new RegionDetail(this));
        this.registerAction(new RegionSize(this));
        this.registerAction(new RegionActive(this));
        this.registerAction(new RegionAccess(this));
        this.registerAction(new RegionOwner(this));
        this.registerAction(new RegionMessage(this));
        this.registerAction(new RegionName(this));
        this.registerAction(new RegionCreate(this));
        this.registerAction(new RegionDefine(this));
        this.registerAction(new RegionDelete(this));
        this.registerAction(new RegionReload(this));
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        Context context = super.parse(this, sender, command, label, args);
        
        boolean owner = false;
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (context.player != null && context.action != null && region != null && region.access.isOwner(context.player)) { 
            // if region owner, then allow certain actions
            // TODO integrate this "better"
            if (Region.OWNER_ALLOWED.contains(context.action.name)) owner = true;
        }
        
        if (!owner) {
            // Standard access checks
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
            
        }
        
        context.action.execute(context);
        
        return true;
    }
    
    @Override
    void parseAction(final Context context) {
        // Check direct action name match in third, second, or first argument. (/<command>[ <World>[ <Region>]]<Action>)
        context.parseAction(2);
    }
    
    // Command Syntax: /region[[ <World>] <Region>][ <Action>][ <Parameters>]
    static World parseWorld(final Context context) {
        // Assume player's world if not specified.
        if (context.actionIndex != 2) {
            if (context.player == null) return null;
            
            return context.player.getWorld();
        }
        
        String name = context.arguments.get(context.actionIndex - 2);
        if (name.equals(edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT)) return null;
        
        return context.owner.plugin.getServer().getWorld(name);
    }
    
    // Command Syntax: /region[[ <World>] <Region>][ <Action>][ <Parameters>]
    static edgruberman.bukkit.simpleregions.Region parseRegion(final Context context) {
        if (context.actionIndex <= 0) {
            // Use current working region if specified
            if (Region.working.containsKey(context.sender)) return Region.working.get(context.sender);
            
            // Assume player's current region if player is in only one
            if (context.player == null) return null;
            
            Set<edgruberman.bukkit.simpleregions.Region> regions = Index.getRegions(context.player.getLocation());
            if (regions.size() != 1) return null;
            
            return regions.iterator().next();
        }
        
        World world = Region.parseWorld(context);
        String name = context.arguments.get(context.actionIndex - 1);
        
        if (name.equals(edgruberman.bukkit.simpleregions.Region.NAME_DEFAULT)) {
            if (world == null) return Index.serverDefault;
            
            return Index.worlds.get(world).worldDefault;
        }
        
        if (world == null) return null;
        
        return Index.worlds.get(world).getRegions().get(name);
    }
    
    static void sendIfOnline(final String name, final MessageLevel level, final String message) {
        if (Region.getExactPlayer(name) == null) return;
        
        Main.messageManager.send(Region.getExactPlayer(name), message, level);
    }
    
    /**
     * Returns player only if it is a full and case insensitive name match.
     *
     * @param name name of player
     * @return player that matches name
     */
    static Player getExactPlayer(final String name) {
        Player player = Bukkit.getServer().getPlayer(name);
        if (player == null) return null;
        
        if (!player.getName().equalsIgnoreCase(name)) return null;
        
        return player;
    }
    
    static boolean isInteger(final String s) {   
        try {   
            Integer.parseInt(s);   
            return true;   
        }   
        catch(Exception e) {   
            return false;   
        }   
    }
}