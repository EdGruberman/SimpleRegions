package edgruberman.bukkit.simpleregions.commands;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionReload extends Action {

    public static final String NAME = "reload";

    RegionReload(final Command owner) {
        super(owner, RegionReload.NAME, Permission.REGION_RELOAD);
    }

    @Override
    void execute(final Context context) {
        Main.configure(true);
        Main.messageManager.tell(context.sender, "Configuration reloaded.", MessageLevel.STATUS, false);
    }
}