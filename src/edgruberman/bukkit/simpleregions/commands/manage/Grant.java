package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class Grant extends OwnerExecutor {

    public Grant(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Access>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String access = RegionExecutor.parse(args, 0, "<Access>", sender);
        if (access == null) return false;

        if (region.access.contains(access)) {
            Main.courier.send(sender, "grant-already", access, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        access = Bukkit.getOfflinePlayer(access).getName();
        region.access.add(access);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "grant", access, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        final Player added = Bukkit.getServer().getPlayerExact(access);
        if (region.active && added != null)
            Main.courier.send(added, "grant-notify", sender.getName(), RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        return true;
    }

}
