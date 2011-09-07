package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionDetail extends Action {
    
    public static final String NAME = "detail";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("info", "details"));
    
    RegionDetail(final Command owner) {
        super(owner, RegionDetail.NAME, Permission.REGION_DETAIL);
        this.aliases.addAll(RegionDetail.ALIASES);
    }
    
    @Override
    void execute(final Context context) {
        edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.respond(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }
        
        Main.messageManager.respond(context.sender, region.describe(RegionDetail.parseFormat(context)), MessageLevel.STATUS, false);
    }
    
    private static Integer parseFormat(final Context context) {
        if (context.actionIndex + 1 > context.arguments.size() - 1) return null;
        
        return Integer.parseInt(context.arguments.get(context.actionIndex + 1));
    }
}