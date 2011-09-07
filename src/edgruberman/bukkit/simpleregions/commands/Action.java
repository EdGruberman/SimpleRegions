package edgruberman.bukkit.simpleregions.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.Permission;

abstract class Action {
    
    Command command;
    String name;
    Permission required;
    String pattern;
    Set<String> aliases = new HashSet<String>();
    
    Action(final Command command, final String name, final Permission required) {
        this(command, name, required, null);
    }
    
    Action(final Command command, final String name, final Permission required, final String pattern) {
        this.command = command;
        this.name = name;
        this.required = required;
        this.pattern = pattern;
    }
    
    protected boolean isAllowed(final CommandSender sender) {
        return sender.hasPermission(this.required.toString());
    }
    
    abstract void execute(final Context context);
}
