package edgruberman.bukkit.simpleregions.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.java.CaseInsensitiveString;

class Context {

    Command owner;
    CommandSender sender;
    Player player;
    String label;
    String line;
    Action action;
    int actionIndex = -1;

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
        this.owner.parseAction(this);
        this.matches = this.parseMatches();

        this.owner.command.getPlugin().getLogger().log(Level.FINEST, "Command Context for " + this.label + "; Arguments: " + this.arguments + "; Action: " + (this.action != null ? this.action.name : null) + "; Matches: " + this.matches);
    }

    /**
     * Parse arguments looking at last possible index first to find action name
     * match (case insensitive).</br>
     * /&lt;command&gt;[[ &lt;Parameters[0...last]&gt;] &lt;Action&gt;])
     *
     * @param last last argument index that can contain action
     */
    void parseAction(final int last) {
        for (this.actionIndex = Math.min(last, this.arguments.size() - 1); this.actionIndex >= 0; this.actionIndex--) {
            this.action = this.owner.actions.get(new CaseInsensitiveString(this.arguments.get(this.actionIndex)));
            if (this.action != null) break;
        }

        if (this.action == null) {
            this.actionIndex = -1;
            this.action = this.owner.defaultAction;
        }
    }

    private Player parsePlayer() {
        if (!(this.sender instanceof Player)) return null;

        return (Player) this.sender;
    }

    private List<String> parseMatches() {
        final List<String> matches = new ArrayList<String>();
        if (this.action == null || this.action.pattern == null)
            return matches;

        final Pattern p = Pattern.compile(this.action.pattern);
        final Matcher m = p.matcher(this.line);
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
    private static List<String> parseArguments(final String[] args) {
        final List<String> arguments = new ArrayList<String>();

        String previous = null;
        for (final String arg : args) {
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