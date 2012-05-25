package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionDetail extends Action {

    public static final String NAME = "detail";
    public static final Set<String> ALIASES = new HashSet<String>(Arrays.asList("info", "details"));

    private final Region base;

    RegionDetail(final Region owner) {
        super(owner, RegionDetail.NAME, Permission.REGION_DETAIL);
        this.base = owner;
        this.aliases.addAll(RegionDetail.ALIASES);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = this.base.parseRegion(context);
        if (region == null) {
            context.respond("Unable to determine region.", MessageLevel.SEVERE);
            return;
        }

        RegionDetail.describe(context, region, RegionDetail.parseFormat(context));
    }

    static void describe(final Context context, final edgruberman.bukkit.simpleregions.Region region) {
        RegionDetail.describe(context, region, null);
    }

    static void describe(final Context context, final edgruberman.bukkit.simpleregions.Region region, final Integer format) {
        for (final String line : region.describe(format).split("\n"))
            context.respond(line, MessageLevel.CONFIG);

        if (!(context.sender.hasPermission(Permission.REGION_DEFINE.toString()) || (context.player != null && region.access.isOwner(context.player))))
            return;

        // Sender has permission, so instruct on how to alter as necessary
        if (!region.isActive()) {
            if (!region.isDefault() && !region.isDefined()) {
                context.respond("Region is undefined. To define: /" + Region.NAME + " " + RegionDefine.NAME, MessageLevel.NOTICE);
                return;
            }

            context.respond("Region is inactive. To activate: /" + Region.NAME + " +" + RegionActive.NAME, MessageLevel.NOTICE);
        }
    }

    private static Integer parseFormat(final Context context) {
        if (context.actionIndex + 1 > context.arguments.size() - 1) return null;

        final String format = context.arguments.get(context.actionIndex + 1);
        if (!Region.isInteger(format)) return null;

        return Integer.parseInt(format);
    }

}
