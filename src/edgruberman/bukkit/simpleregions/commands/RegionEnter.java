package edgruberman.bukkit.simpleregions.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.BoundaryAlerter;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.messaging.Message;
import edgruberman.bukkit.simpleregions.messaging.recipients.Sender;

public class RegionEnter extends RegionExecutor {

    private final BoundaryAlerter alerter;

    public RegionEnter(final Catalog catalog, final BoundaryAlerter alerter) {
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
        for (final Message message : this.alerter.createMessage(region.enter, "enter", "enterCustom", region, sender))
            Main.courier.submit(new Sender(sender), message);

        return true;
    }

}
