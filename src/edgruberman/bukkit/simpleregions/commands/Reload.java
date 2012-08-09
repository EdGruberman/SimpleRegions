package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.simpleregions.Main;

public class Reload implements CommandExecutor {

    private final Plugin plugin;

    public Reload(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        this.plugin.reloadConfig();
        Main.courier.send(sender, "reload");
        // TODO reload all world specific region config files
        return true;
    }

}
