package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.simpleregions.Permission;
import edgruberman.bukkit.simpleregions.util.CaseInsensitiveString;

abstract class Command  {

    Map<CaseInsensitiveString, Action> actions = new HashMap<CaseInsensitiveString, Action>();
    Action defaultAction = null;

    protected final JavaPlugin plugin;
    protected final String name;
    protected final Permission required;

    protected PluginCommand command = null;

    protected Command(final JavaPlugin plugin, final String name, final Permission required) {
        this.plugin = plugin;
        this.name = name;
        this.required = required;
    }

    protected Context parse(final Command owner, final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        this.plugin.getLogger().log(Level.FINE,
                ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                    + " issued command: " + label + " " + Command.join(args)
        );

        return new Context(this, sender, command, label, args);
    }

    protected boolean isAllowed(final CommandSender sender) {
        return sender.hasPermission(this.required.toString());
    }

    protected void registerAction(final Action action) {
        this.registerAction(action, false);
    }

    protected void registerAction(final Action action, final boolean isDefault) {
        if (action == null)
            throw new IllegalArgumentException("Action can not be null.");

        if (this.actions.containsKey(action.name))
            throw new IllegalArgumentException("Action " + action.name + " already registered.");

        this.actions.put(action.name, action);
        for (final String alias : action.aliases)
            this.actions.put(new CaseInsensitiveString(alias), action);

        if (isDefault) this.defaultAction = action;
    }

    /**
     * Registers executor for a command.
     *
     * @param name command label to register
     */
    protected void setExecutorOf(final CommandExecutor executor) {
        this.command = this.plugin.getCommand(this.name);
        if (this.command == null) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to register \"" + this.name + "\" command.");
            return;
        }

        this.command.setExecutor(executor);
    }

    abstract void parseAction(final Context context);

    /**
     * Concatenate all string elements of an array together with a space.
     *
     * @param s string array
     * @return concatenated elements
     */
    static String join(final String[] s) {
        return Command.join(Arrays.asList(s), " ");
    }

    /**
     * Combine all the elements of a list together with a delimiter between each.
     *
     * @param list list of elements to join
     * @param delim delimiter to place between each element
     * @return string combined with all elements and delimiters
     */
    static String join(final List<String> list, final String delim) {
        if (list == null || list.isEmpty()) return "";

        final StringBuilder sb = new StringBuilder();
        for (final String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());

        return sb.toString();
    }
}
