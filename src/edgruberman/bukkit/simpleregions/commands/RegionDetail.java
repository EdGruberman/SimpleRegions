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
        final edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null) {
            Main.messageManager.tell(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }

        RegionDetail.describe(context, region, RegionDetail.parseFormat(context));
    }

    static void describe(final Context context, final edgruberman.bukkit.simpleregions.Region region) {
        RegionDetail.describe(context, region, null);
    }

    static void describe(final Context context, final edgruberman.bukkit.simpleregions.Region region, final Integer format) {
        Main.messageManager.tell(context.sender, region.describe(format), MessageLevel.CONFIG, false);

        if (!(context.sender.hasPermission(Permission.REGION_DEFINE.toString()) || (context.player != null && region.access.isOwner(context.player))))
            return;

        // Sender has permission, so instruct on how to alter as necessary
        if (!region.isActive()) {
            if (!region.isDefault() && !region.isDefined()) {
                Main.messageManager.tell(context.sender, "Region is undefined. To define: /" + Region.NAME + " " + RegionDefine.NAME, MessageLevel.NOTICE, false);
                return;
            }

            Main.messageManager.tell(context.sender, "Region is inactive. To activate: /" + Region.NAME + " +" + RegionActive.NAME, MessageLevel.NOTICE, false);
        }
    }

    private static Integer parseFormat(final Context context) {
        if (context.actionIndex + 1 > context.arguments.size() - 1) return null;

        final String format = context.arguments.get(context.actionIndex + 1);
        if (!Region.isInteger(format)) return null;

        return Integer.parseInt(format);
    }
}