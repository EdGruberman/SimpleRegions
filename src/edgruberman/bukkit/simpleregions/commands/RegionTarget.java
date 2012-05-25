package edgruberman.bukkit.simpleregions.commands;

import java.util.HashSet;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionTarget extends Action {

    public static final String NAME = "target";

    private final RegionCurrent regionCurrent;

    RegionTarget(final Command owner, final RegionCurrent regionCurrent) {
        super(owner, RegionTarget.NAME, Permission.REGION_TARGET);
        this.regionCurrent = regionCurrent;
    }

    @Override
    void execute(final Context context) {
        if (context.player == null) {
            context.respond("Invalid action from console", MessageLevel.SEVERE);
            return;
        }

        this.regionCurrent.message(context, context.player.getTargetBlock((HashSet<Byte>) null, 50).getLocation());
    }

}
