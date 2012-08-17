package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.BoundaryAlerter;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.messaging.Sender;

public class RegionExit extends RegionExecutor {

    private final BoundaryAlerter alerter;

    public RegionExit(final Catalog catalog, final BoundaryAlerter alerter) {
        super(catalog, -1, true);
        this.alerter = alerter;
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
        Main.courier.submit(new Sender(sender), this.alerter.draft(region.exit, "exit", "exitCustom", region, sender));

        return true;
    }

}
