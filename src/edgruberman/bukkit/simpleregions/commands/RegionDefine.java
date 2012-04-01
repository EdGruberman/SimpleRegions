package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Permission;

public class RegionDefine extends Action {

    public static final String NAME = "define";

    RegionDefine(final Command owner) {
        super(owner, RegionDefine.NAME, Permission.REGION_DEFINE);
    }

    @Override
    void execute(final Context context) {
        final edgruberman.bukkit.simpleregions.Region region = Region.parseRegion(context);
        if (region == null || region.isDefault()) {
            Main.messageManager.tell(context.sender, "Unable to determine region.", MessageLevel.SEVERE, false);
            return;
        }

        final List<String> parameters = context.arguments.subList(context.actionIndex + 1, context.arguments.size());
        Block block = null;
        if (context.player != null) block = context.player.getTargetBlock((HashSet<Byte>) null, 100);

        if (parameters.size() == 0) {
            if (block == null) {
                Main.messageManager.tell(context.sender, "Unable to determine target block.", MessageLevel.WARNING, false);
                return;
            }

            if (region.getX1() == null) {
                region.setX1(block.getX());
                region.setY1(block.getY());
                region.setZ1(block.getZ());
            } else if (region.getX2() == null) {
                region.setX2(block.getX());
                region.setY2(block.getY());
                region.setZ2(block.getZ());
            } else {
                region.setX1(region.getX2());
                region.setY1(region.getY2());
                region.setZ1(region.getZ2());
                region.setX2(block.getX());
                region.setY2(block.getY());
                region.setZ2(block.getZ());
            }

        } else {
            final String type = parameters.get(0).toUpperCase();
            if (Arrays.asList("1", "2", "N", "E", "S", "W", "U", "D").contains(type)) {
                if (type.equals("1")) {
                    region.setX1(block.getX());
                    region.setY1(block.getY());
                    region.setZ1(block.getZ());
                } else if (type.equals("2")) {
                    region.setX2(block.getX());
                    region.setY2(block.getY());
                    region.setZ2(block.getZ());
                } else if (type.equals("N")) { region.setN(block.getX());
                } else if (type.equals("E")) { region.setE(block.getZ());
                } else if (type.equals("S")) { region.setS(block.getX());
                } else if (type.equals("W")) { region.setW(block.getZ());
                } else if (type.equals("U")) { region.setU(block.getY());
                } else if (type.equals("D")) { region.setD(block.getY());
                }

            } else {
                if (!parameters.get(0).contains(":")) {
                    Main.messageManager.tell(context.sender, "Parameters must be recognizable key:value pairs.", MessageLevel.SEVERE, false);
                    return;
                }

                // First parameter contains a colon, so look through the rest for more coordinates also.
                String key; int value;
                for (final String coord : parameters) {
                    if (!coord.contains(":")) continue;

                    if (!Region.isInteger(coord.split(":")[1])) continue;

                    value = Integer.parseInt(coord.split(":")[1]);
                    key = coord.split(":")[0].toLowerCase();
                         if (key.equals("x1")) { region.setX1(value); }
                    else if (key.equals("x2")) { region.setX2(value); }
                    else if (key.equals("y1")) { region.setY1(value); }
                    else if (key.equals("y2")) { region.setY2(value); }
                    else if (key.equals("z1")) { region.setZ1(value); }
                    else if (key.equals("z2")) { region.setZ2(value); }
                    else if (key.equals("n")) { region.setN(value); }
                    else if (key.equals("e")) { region.setE(value); }
                    else if (key.equals("s")) { region.setS(value); }
                    else if (key.equals("w")) { region.setW(value); }
                    else if (key.equals("u")) { region.setU(value); }
                    else if (key.equals("d")) { region.setD(value); }
                }
            }
        }

        Main.saveRegion(region, false);

        // Show configuration of region after update.
        RegionDetail.describe(context, region);
        Main.messageManager.tell(context.sender, region.describeArea(), MessageLevel.CONFIG, false);
        Main.messageManager.tell(context.sender, region.describeVolume(), MessageLevel.CONFIG, false);
        Main.messageManager.tell(context.sender, "Region coordinate definition updated.", MessageLevel.STATUS, false);

        return;
    }

    // Command Syntax: /region create[ <World>] <Region>
    static World parseWorld(final Context context) {
        // Assume player's world if not specified.
        if (context.arguments.size() <= 2) {
            if (context.player == null) return null;

            return context.player.getWorld();
        }

        final String name = context.arguments.get(context.actionIndex + 1);
        if (name.equals(edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT)) return null;

        return context.owner.plugin.getServer().getWorld(name);
    }
}