package edgruberman.bukkit.simpleregions.commands.design;

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
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Define extends RegionExecutor {

    public Define(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command>[ (1|2)|((n|e|s|w|u|d)[:<Coordinate>])[ <Region>[ <World>]]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        Block block = null;
        if (sender instanceof Player) block = ((Player) sender).getTargetBlock((HashSet<Byte>) null, 100);

        if (args.size() == 0) {
            if (block == null) {
                Main.courier.send(sender, "unknown-argument", "<Block>", block);
                return false;
            }

            if (region.getX1() == null) {
                region.setVertex1(block.getX(), block.getY(), block.getZ());
            } else if (region.getX2() == null) {
                region.setVertex2(block.getX(), block.getY(), block.getZ());
            } else {
                // neither are null, assume 2 was the last set and move that into 1 then put new coords into 2
                region.setVertex1(region.getX2(), region.getY2(), region.getZ2());
                region.setVertex2(block.getX(), block.getY(), block.getZ());
            }

        } else {
            final String type = args.get(0).split(":")[0].toLowerCase();
            if (!Arrays.asList("1", "2", "n", "e", "s", "w", "u", "d").contains(type)) {
                Main.courier.send(sender, "unknown-argument", "<Type>", type);
                return false;

            }

            if (args.get(0).split(":").length == 1) {
                     if (type.equals("1")) { region.setVertex1(block.getX(), block.getY(), block.getZ()); }
                else if (type.equals("2")) { region.setVertex2(block.getX(), block.getY(), block.getZ()); }
                else if (type.equals("n")) { region.setMinZ(block.getZ()); }
                else if (type.equals("e")) { region.setMaxX(block.getX()); }
                else if (type.equals("s")) { region.setMaxZ(block.getZ()); }
                else if (type.equals("w")) { region.setMinX(block.getX()); }
                else if (type.equals("u")) { region.setMaxY(block.getY()); }
                else if (type.equals("d")) { region.setMinY(block.getY()); }

            } else {
                int coord;
                try {
                    coord = Integer.parseInt(args.get(0).split(":")[1]);
                } catch (final NumberFormatException e) {
                    Main.courier.send(sender, "unknown-argument", "<Coordinate>", args.get(0));
                    return false;
                }

                if (type.equals("1") || type.equals("2")) {
                    Main.courier.send(sender, "unknown-argument", "<Type>", type);
                    return false;
                }
                else if (type.equals("n")) { region.setMinZ(coord); }
                else if (type.equals("e")) { region.setMaxX(coord); }
                else if (type.equals("s")) { region.setMaxZ(coord); }
                else if (type.equals("w")) { region.setMinX(coord); }
                else if (type.equals("u")) { region.setMaxY(coord); }
                else if (type.equals("d")) { region.setMinY(coord); }
            }
        }

        this.catalog.indices.get(region.world).refresh(region);
        this.catalog.repository.saveRegion(region, false);
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:describe " + RegionExecutor.formatName(region) + " " + RegionExecutor.formatWorld(region));
        Main.courier.send(sender, "define");
        return true;
    }

}
