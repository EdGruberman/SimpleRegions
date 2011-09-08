package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionSet extends Action {
    
    public static final String NAME = "set";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("+set", "unset", "-set"));
    
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
        
        String operation = context.arguments.get(context.actionIndex).substring(0, 1);
        boolean set = (operation.equals("+") || operation.equals("s"));
        RegionSet.setWorkingRegion(context.sender, region, set);
    }
    
    static boolean setWorkingRegion(final CommandSender sender, final edgruberman.bukkit.simpleregions.Region region, final boolean set) {
        String world = (region.getWorld() == null ? edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT : region.getWorld().getName());
        
        // Set Operation
        if (set) {
            Region.working.put(sender, region);
            Main.messageManager.respond(sender, "Working region set to: [" + world + "] " + region.getDisplayName(), MessageLevel.STATUS, false);
            return true;
        }
        
        // Unset Operation
        if (!Region.working.containsKey(sender)) {
            Main.messageManager.respond(sender, "Working region not currently set.", MessageLevel.WARNING, false);
            return false;
        }
        
        Region.working.remove(sender);
        Main.messageManager.respond(sender, "Working region unset from: [" + world + "] " + region.getDisplayName(), MessageLevel.STATUS, false);
        return true;
    }
}