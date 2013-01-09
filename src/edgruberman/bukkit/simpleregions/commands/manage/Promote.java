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

public class Promote extends OwnerExecutor {

    public Promote(final Catalog catalog) {
        super(catalog, 1);
    }

    // usage: /<command> <Owner>[ <Region>[ <World>]]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        String owner = RegionExecutor.parse(args, 0, "<Owner>", sender);
        if (owner == null) return false;

        if (region.owners.contains(owner)) {
            Main.courier.send(sender, "promote-already", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));
            return true;
        }

        owner = Bukkit.getOfflinePlayer(owner).getName();
        region.owners.add(owner);
        this.catalog.repository.saveRegion(region, false);
        Main.courier.send(sender, "promote", owner, RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        final Player added = Bukkit.getServer().getPlayerExact(owner);
        if (region.active && added != null)
            Main.courier.send(added, "promote-notify", sender.getName(), RegionExecutor.formatName(region), RegionExecutor.formatWorld(region));

        return true;
    }

}
