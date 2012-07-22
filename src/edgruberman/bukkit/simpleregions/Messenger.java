package edgruberman.bukkit.simpleregions;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.channels.Recipient;

public class Messenger {

    private static Messenger instance = null;

    public static Messenger load(final Plugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("MessageManager") != null) {
            Messenger.instance = new MessageManagerMessenger(plugin);
            plugin.getLogger().config("Message timestamps will use MessageManager plugin for personalized player time zones");
        } else {
            Messenger.instance = new Messenger(plugin);
            plugin.getLogger().config("Message timestamps will use server time zone: " + Messenger.instance.zone.getDisplayName());
        }

        return Messenger.instance;
    }

    public static String tell(final CommandSender target, final String path, final Object... args) {
        return Messenger.instance.sendMessage(target, Messenger.instance.formats.getString(path), args);
    }

    public static int broadcast(final String permission, final String format, final Object... args) {
        return Messenger.instance.broadcastMessage(permission, format, args);
    }

    public static int broadcast(final String format, final Object... args) {
        return Messenger.instance.broadcastMessage(format, args);
    }

    public static String getFormat(final String path) {
        return Messenger.instance.formats.getString(path);
    }

    public static Messenger get() {
        return Messenger.instance;
    }

    public ConfigurationSection formats;

    protected final Plugin plugin;
    protected final TimeZone zone = TimeZone.getDefault();

    private Messenger(final Plugin plugin) {
        this.plugin = plugin;
        this.formats = plugin.getConfig().getConfigurationSection("messages");
    }

    public TimeZone getZone(final CommandSender target) {
        return this.zone;
    }

    public String sendMessage(final CommandSender target, final String format, final Object... args) {
        final String message = this.sendMessage(new GregorianCalendar(), target, format, args);
        this.plugin.getLogger().finer("#PLAYER(" + target.getName() + ")# " + message);
        return message;
    }

    private String sendMessage(final Calendar now, final CommandSender target, final String format, final Object... args) {
        now.setTimeZone(this.getZone(target));
        final String message = this.format(now, format, args);
        target.sendMessage(message);
        return message;
    }

    private String format(final Calendar now, final String format, final Object... args) {
        // Prepend time argument
        Object[] argsAll = null;
        argsAll = new Object[args.length + 1];
        argsAll[0] = now;
        if (args.length >= 1) System.arraycopy(args, 0, argsAll, 1, args.length);

        // Format message
        String message = ChatColor.translateAlternateColorCodes('&', format);
        message = String.format(message, argsAll);

        return message;
    }

    /**
     * Send a message to all players with the specific permission
     */
    public int broadcastMessage(final String permission, final String format, final Object... args) {
        final Calendar now = new GregorianCalendar();
        now.setTimeZone(this.zone);

        int count = 0;
        for (final Permissible permissible : Bukkit.getPluginManager().getPermissionSubscriptions(permission))
            if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                this.sendMessage(now, (CommandSender) permissible, format, args);
                count++;
            }

        this.plugin.getLogger().finer("#BROADCAST(" + permission + ")" + count + ")# " + this.format(now, format, args));

        return count;
    }

    /**
     * Send a message to all players with the Server.BROADCAST_CHANNEL_USERS permission
     */
    public int broadcastMessage(final String format, final Object... args) {
        return this.broadcastMessage(Server.BROADCAST_CHANNEL_USERS, format, args);
    }

    private static class MessageManagerMessenger extends Messenger {

        MessageManagerMessenger(final Plugin plugin) {
            super(plugin);
        }

        @Override
        public TimeZone getZone(final CommandSender target) {
            final ConfigurationSection section = Recipient.configurationFile.getConfig().getConfigurationSection("CraftPlayer." + target.getName());
            if (section != null) return TimeZone.getTimeZone(section.getString("timezone", this.zone.getID()));

            return this.zone;
        }

        // TODO Monitor PluginEnable/Disable event to update

    }

}
