package edgruberman.bukkit.simpleregions.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.simpleregions.Main;

class Context {
    
    Command owner;
    CommandSender sender;
    Player player;
    String label;
    String line;
    Action action;
    int actionIndex;
    
    /**
     * Command line split using double quotes to distinguish single arguments.
     */
    List<String> arguments;
    
    /**
     * Groupings found according to action's regular expression pattern.
     */
    List<String> matches;
    
    Context(final Command owner, final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        this.owner = owner;
        this.sender = sender;
        this.player = this.parsePlayer();
        this.label = label;
        this.line = Command.join(args);
        this.arguments = Context.parseArguments(args);
        this.parseAction();
        this.matches = this.parseMatches();
        
        Main.messageManager.log("Command Context for " + this.label + "; Arguments: " + this.arguments + "; Action: " + (this.action != null ? this.action.name : null) + "; Matches: " + this.matches, MessageLevel.FINEST);
    }
    
    private void parseAction() {
        // Check direct action name match in third, second, or first argument. (/<command>[ <World>[ <Region>]]<Action>)
        if (this.arguments.size() >= 3 && this.owner.actions.containsKey(this.arguments.get(2))) {  
            this.actionIndex = 2;
        } else if (this.arguments.size() >= 2 && this.owner.actions.containsKey(this.arguments.get(1))) {  
            this.actionIndex = 1;
        } else if (this.arguments.size() >= 1 && this.owner.actions.containsKey(this.arguments.get(0))) {    
            this.actionIndex = 0;
        } else {
            this.actionIndex = -1;
        }
        
        if (this.actionIndex >= 0) {
            this.action = this.owner.actions.get(this.arguments.get(this.actionIndex));
        } else {  
            this.action = this.owner.defaultAction;
        }
    }
    
    private Player parsePlayer() {
        if (!(this.sender instanceof Player)) return null;
        
        return (Player) this.sender;
    }
    
    private List<String> parseMatches() {
        List<String> matches = new ArrayList<String>();
        if (this.action == null || this.action.pattern == null)
            return matches;
        
        Pattern p = Pattern.compile(this.action.pattern);
        Matcher m = p.matcher(this.line);
        if (m.find())
            for (int i = 1; i <= m.groupCount(); i++)
                matches.add(m.group(i));
        
        return matches;
    }
    
    /**
     * Concatenate arguments to compensate for double quotes indicating single
     * argument, removing any delimiting double quotes.
     *  
     * @return arguments
     * @TODO use / for escaping double quote characters
     */
    private static List<String> parseArguments(String[] args) {
        List<String> arguments = new ArrayList<String>();
        
        String previous = null;
        for (String arg : args) {
            if (previous != null) {
                if (arg.endsWith("\"")) {
                    arguments.add(Context.stripDoubleQuotes(previous + " " + arg));
                    previous = null;
                } else {
                    previous += " " + arg;
                }
                continue;
            }

            if (arg.startsWith("\"") && !arg.endsWith("\"")) {
                previous = arg;
            } else {
                arguments.add(Context.stripDoubleQuotes(arg));
            }
        }
        if (previous != null) arguments.add(Context.stripDoubleQuotes(previous));
        
        return arguments;
    }
    
    private static String stripDoubleQuotes(final String s) {
        return Context.stripDelimiters(s, "\"");
    }
    
    private static String stripDelimiters(final String s, final String delim) {
        if (!s.startsWith(delim) || !s.endsWith(delim)) return s;
        
        return s.substring(1, s.length() - 1);
    }
}