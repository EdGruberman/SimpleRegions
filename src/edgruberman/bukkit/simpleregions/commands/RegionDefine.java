package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDefine extends RegionExecutor {

    public RegionDefine(final Catalog catalog) {
        super(catalog, 2, false);
    }

    // usage: /<command>[ (n|e|s|w|u|d|1|2)[ <Coordinate>[ <Region>[ <World>]]]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        Block block = null;
        if (sender instanceof Player) block = ((Player) sender).getTargetBlock((HashSet<Byte>) null, 100);

        if (args.size() == 0) {
            if (block == null) {
                Main.messenger.tell(sender, "blockNotIdentified");
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
            final String type = args.get(0).toLowerCase();
            if (!Arrays.asList("n", "e", "s", "w", "u", "d", "1", "2").contains(type)) {
                Main.messenger.tell(sender, "coordinateNotIdentified", type);
                return false;

            }

            if (args.size() <= 1) {
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
                    coord = Integer.parseInt(args.get(1));
                } catch (final NumberFormatException e) {
                    Main.messenger.tell(sender, "coordinateNotIdentified", args.get(1));
                    return false;
                }

                if (type.equals("1")) {
                    Main.messenger.tell(sender, "unsupportedParameter", "1", "Specific coordinate must designate specific direction");
                    return false;
                } else if (type.equals("2")) {
                    Main.messenger.tell(sender, "unsupportedParameter", "2", "Specific coordinate must designate specific direction");
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
        Main.messenger.tell(sender, "coordinateUpdated");
        return true;
    }

}
