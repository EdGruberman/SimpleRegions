package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Region;

public class RegionDefine implements CommandExecutor {

    private final Plugin plugin;
    private final Catalog catalog;

    public RegionDefine(final Plugin plugin, final Catalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    // usage: /<command>[ (n|e|s|w|u|d|1|2)[ <Coordinate>[ <Region>[ <World>]]]]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Region region = Utility.parseRegion(this.plugin, this.catalog, sender, args, 1);
        if (region == null) return false;

        Block block = null;
        if (sender instanceof Player) block = ((Player) sender).getTargetBlock((HashSet<Byte>) null, 100);

        if (args.length == 0) {
            if (block == null) {
                MessageManager.of(this.plugin).tell(sender, "Target block §enot identified§r", MessageLevel.WARNING, false);
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
                MessageManager.of(this.plugin).tell(sender, String.format("Coordinate type %1$s §enot recognized§r", type), MessageLevel.SEVERE, false);
                return false;

            } else {
                       if (type.equals("1")) { region.set1(block.getX(), block.getY(), block.getZ());
                } else if (type.equals("2")) { region.set2(block.getX(), block.getY(), block.getZ());
                } else if (type.equals("n")) { region.setN(block.getZ());
                } else if (type.equals("e")) { region.setE(block.getX());
                } else if (type.equals("s")) { region.setS(block.getZ());
                } else if (type.equals("w")) { region.setW(block.getX());
                } else if (type.equals("u")) { region.setU(block.getY());
                } else if (type.equals("d")) { region.setD(block.getY());
                }
            }
        }

        this.catalog.repository.saveRegion(region, false);
        Bukkit.getServer().dispatchCommand(sender, "simpleregions:region.info " + region.getDisplayName() + " " + region.world.getName());
        MessageManager.of(this.plugin).tell(sender, "Region coordinates updated", MessageLevel.STATUS, false);

        return true;
    }

}
