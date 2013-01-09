package edgruberman.bukkit.simpleregions.commands.manage;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.Executor;

public class Farewell extends OwnerExecutor {

    public Farewell(final Catalog catalog) {
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
            Main.courier.send(sender, "farewell-previous", (region.farewell != null ? region.farewell : "§onull§r"));
            if (args.get(0).equals("default")) {
                region.farewell = null;
            } else {
                region.farewell = Executor.join(args, " ");
                if (sender.hasPermission("simpleregions.message.color")) region.farewell = ChatColor.translateAlternateColorCodes('&', region.farewell);
            }
            this.catalog.repository.saveRegion(region, false);
            Main.courier.send(sender, "farewell");
        }

        Main.courier.send(sender, "farewell-existing", (region.farewell != null ? region.farewell : "§onull§r"));
        Main.courier.send(sender, "farewell-example");
        if (region.farewell == null) {
            Main.courier.send(sender, "farewell-default", region.name, region.hasAccess(sender)?1:0);
        } else if (region.farewell.length() > 0) {
            Main.courier.send(sender, "farewell-custom", region.name, region.hasAccess(sender)?1:0, region.farewell);
        }

        return true;
    }

}
