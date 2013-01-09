package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionExit extends RegionExecutor {

    public RegionExit(final Catalog catalog) {
        super(catalog, -1, true);
        this.tokenizer.setIgnoreEmptyTokens(false);
    }

    // usage: /<command>[ <Message>]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        if (region == null) {
            Main.courier.send(sender, "requiresWorkingRegion");
            return true;
        }

        if (args.size() >= 1) {
            Main.courier.send(sender, "exitPrevious", (region.exit != null ? region.exit : "§onull§r"));
            if (args.get(0).equals("default")) {
                region.exit = null;
            } else {
                region.exit = Executor.join(args, " ");
                if (sender.hasPermission("simpleregions.region.message.color")) region.exit = ChatColor.translateAlternateColorCodes('&', region.exit);
            }
            this.catalog.repository.saveRegion(region, false);
            Main.courier.send(sender, "exitSuccess");
        }

        Main.courier.send(sender, "exitExisting", (region.exit != null ? region.exit : "§onull§r"));
        Main.courier.send(sender, "exitExample");
        if (region.exit == null) {
            Main.courier.send(sender, "exit", region.name, region.hasAccess(sender)?1:0);
        } else if (region.exit.length() > 0) {
            Main.courier.send(sender, "exitCustom", region.name, region.hasAccess(sender)?1:0, region.exit);
        }

        return true;
    }

}
