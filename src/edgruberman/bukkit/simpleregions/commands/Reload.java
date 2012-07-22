package edgruberman.bukkit.simpleregions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Messenger;

public class Reload implements CommandExecutor {

    private final Plugin plugin;

    public Reload(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Main main = (Main) this.plugin;
        main.reloadConfig();
        main.start(this.plugin);
        Messenger.tell(sender, "reload");
        return true;
    }

}
