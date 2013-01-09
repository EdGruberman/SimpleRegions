package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.Executor;

public class Greeting extends OwnerExecutor {

    public Greeting(final Catalog catalog) {
        super(catalog, -1);
        this.tokenizer.setIgnoreEmptyTokens(false);
    }

    // usage: /<command>[ <Message>]
    @Override
    protected boolean perform(final CommandSender sender, final Command command, final String label, final List<String> args, final Region region) {
        if (region == null) {
            Main.courier.send(sender, "requires-working-region");
            return true;
        }

        if (args.size() >= 1) {
            Main.courier.send(sender, "greeting-previous", (region.greeting != null ? region.greeting : "§onull§r"));
            if (args.get(0).equals("default")) {
                region.greeting = null;
            } else {
                region.greeting = Executor.join(args, " ");
                if (sender.hasPermission("simpleregions.message.color")) region.greeting = ChatColor.translateAlternateColorCodes('&', region.greeting);
            }
            this.catalog.repository.saveRegion(region, false);
            Main.courier.send(sender, "greeting");
        }

        Main.courier.send(sender, "greeting-existing", (region.greeting != null ? region.greeting : "§onull§r"));
        Main.courier.send(sender, "greeting-example");
        if (region.greeting == null) {
            Main.courier.send(sender, "greeting-default", region.name, region.hasAccess(sender)?1:0);
        } else if (region.greeting.length() > 0) {
            Main.courier.send(sender, "greeting-custom", region.name, region.hasAccess(sender)?1:0, region.greeting);
        }

        return true;
    }

}
