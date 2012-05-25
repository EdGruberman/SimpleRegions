package edgruberman.bukkit.simpleregions.commands;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionReload extends Action {

    public static final String NAME = "reload";

    private final Region base;

    RegionReload(final Region owner) {
        super(owner, RegionReload.NAME, Permission.REGION_RELOAD);
        this.base = owner;
    }

    @Override
    void execute(final Context context) {
        final Main main = (Main) this.base.plugin;
        main.reloadConfig();
        main.start(this.base.plugin);
        context.respond("Configuration reloaded", MessageLevel.STATUS);
    }

}
