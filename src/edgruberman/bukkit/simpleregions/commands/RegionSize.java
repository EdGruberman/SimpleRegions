package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionSize extends Action {

    public static final String NAME = "size";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("volume", "vol", "area", "blocks"));

    RegionSize(final Command owner) {
        super(owner, RegionSize.NAME, Permission.REGION_SIZE);
        this.aliases.addAll(RegionSize.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null || region.isDefault()) {
            Main.messageManager.tell(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }

        Main.messageManager.tell(context.sender, "-- Region size for " + region.getDisplayName() + ":", MessageLevel.CONFIG, false);
        Main.messageManager.tell(context.sender, region.describeArea(), MessageLevel.CONFIG, false);
        Main.messageManager.tell(context.sender, region.describeVolume(), MessageLevel.CONFIG, false);
    }
}