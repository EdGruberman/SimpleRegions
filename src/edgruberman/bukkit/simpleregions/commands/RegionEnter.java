package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;

public class RegionEnter extends RegionExecutor {

    public RegionEnter(final Catalog catalog) {
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
            Main.courier.send(sender, "enterPrevious", (region.enter != null ? region.enter : "§onull§r"));
            if (args.get(0).equals("default")) {
                region.enter = null;
            } else {
                region.enter = Executor.join(args, " ");
                if (sender.hasPermission("simpleregions.region.message.color")) region.enter = ChatColor.translateAlternateColorCodes('&', region.enter);
            }
            this.catalog.repository.saveRegion(region, false);
            Main.courier.send(sender, "enterSuccess");
        }

        Main.courier.send(sender, "enterExisting", (region.enter != null ? region.enter : "§onull§r"));
        Main.courier.send(sender, "enterExample");
        if (region.enter == null) {
            Main.courier.send(sender, "enter", region.name, region.hasAccess(sender)?1:0);
        } else if (region.enter.length() > 0) {
            Main.courier.send(sender, "enterCustom", region.name, region.hasAccess(sender)?1:0, region.enter);
        }

        return true;
    }

}
