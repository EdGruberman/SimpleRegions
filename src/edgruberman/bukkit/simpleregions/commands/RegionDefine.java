package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Messenger;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDefine implements CommandExecutor {

    private final Catalog catalog;

    public RegionDefine(final Catalog catalog) {
        this.catalog = catalog;
    }

    // usage: /<command>[ (n|e|s|w|u|d|1|2)[ <Coordinate>[ <Region>[ <World>]]]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(args, 2, this.catalog, sender);
        if (region == null) return false;

        Block block = null;
        if (sender instanceof Player) block = ((Player) sender).getTargetBlock((HashSet<Byte>) null, 100);

        if (args.length == 0) {
            if (block == null) {
                Messenger.tell(sender, "blockNotIdentified");
                return false;
            }

            if (region.getX1() == null) {
                region.set1(block.getX(), block.getY(), block.getZ());
            } else if (region.getX2() == null) {
                region.set2(block.getX(), block.getY(), block.getZ());
            } else {
                // Neither are null, assume 2 was the last set and move that into 1 then put new coords into 2
                region.set1(region.getX2(), region.getY2(), region.getZ2());
                region.set2(block.getX(), block.getY(), block.getZ());
            }

        } else {
            final String type = args[0].toLowerCase();
            if (!Arrays.asList("n", "e", "s", "w", "u", "d", "1", "2").contains(type)) {
                Messenger.tell(sender, "coordinateNotIdentified", type);
                return false;

            }

            if (args.length <= 1) {
                       if (type.equals("1")) { region.set1(block.getX(), block.getY(), block.getZ());
                } else if (type.equals("2")) { region.set2(block.getX(), block.getY(), block.getZ());
                } else if (type.equals("n")) { region.setN(block.getZ());
                } else if (type.equals("e")) { region.setE(block.getX());
                } else if (type.equals("s")) { region.setS(block.getZ());
                } else if (type.equals("w")) { region.setW(block.getX());
                } else if (type.equals("u")) { region.setU(block.getY());
                } else if (type.equals("d")) { region.setD(block.getY());
                }

            } else {
                int coord;
                try {
                    coord = Integer.parseInt(args[1]);
                } catch (final NumberFormatException e) {
                    Messenger.tell(sender, "coordinateNotIdentified", args[1]);
                    return false;
                }

                if (type.equals("1")) {
                    Messenger.tell(sender, "unsupportedParameter", "1", "Specific coordinate must designate specific direction");
                    return false;
                } else if (type.equals("2")) {
                    Messenger.tell(sender, "unsupportedParameter", "2", "Specific coordinate must designate specific direction");
                    return false;
                } else if (type.equals("n")) { region.setN(coord);
                } else if (type.equals("e")) { region.setE(coord);
                } else if (type.equals("s")) { region.setS(coord);
                } else if (type.equals("w")) { region.setW(coord);
                } else if (type.equals("u")) { region.setU(coord);
                } else if (type.equals("d")) { region.setD(coord);
                }
            }
        }

        this.catalog.repository.saveRegion(region, false);
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.info " + region.formatName() + " " + region.formatWorld());
        Messenger.tell(sender, "coordinateUpdated");
        return true;
    }

}
