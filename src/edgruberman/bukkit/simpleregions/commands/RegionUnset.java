package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionUnset extends Action {
    
    public static final String NAME = "unset";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("-set"));
    
    RegionUnset(final Command owner) {
        super(owner, RegionUnset.NAME, Permission.REGION_SET);
        this.aliases.addAll(RegionUnset.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        Region.working.remove(context.sender);
        
        Main.messageManager.respond(context.sender, "Working region unset.", MessageLevel.STATUS, false);
    }
}