package edgruberman.bukkit.simpleregions.commands;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionTarget extends Action {
    
    public static final String NAME = "target";
    
    RegionTarget(final Command owner) {
        super(owner, RegionTarget.NAME, Permission.REGION_TARGET);
    }
    
    @Override
    void execute(final Context context) {
        if (context.player == null) {
            Main.messageManager.respond(context.sender, "Invalid action from console.", MessageLevel.SEVERE, false);
            return;
        }
        
        RegionCurrent.message(context, context.player.getTargetBlock(null, 50).getLocation());
    }
}