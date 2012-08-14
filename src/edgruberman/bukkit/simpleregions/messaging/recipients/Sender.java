package edgruberman.bukkit.simpleregions.messaging.recipients;

import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.messaging.Message;
import edgruberman.bukkit.simpleregions.messaging.Recipients;
import edgruberman.bukkit.simpleregions.messaging.messages.Confirmation;

/**
 * individual {@link org.bukkit.command.CommandSender CommandSender}
 *
 * @author EdGruberman (ed@rjump.com)
 * @version 1.0.0
 */
public class Sender implements Recipients {

    protected CommandSender target;

    public Sender(final CommandSender target) {
        this.target = target;
    }

    @Override
    public Confirmation deliver(final Message message) {
        final String formatted = message.format(this.target).toString();
        this.target.sendMessage(formatted);
        return new Confirmation(this.level(), 1
                , "[SEND@{1}] {0}", message, Sender.this.target.getName());
    }

    /** console messages will be FINEST to allow for easier filtering of messages that will already appear in console */
    private Level level() {
        return (this.target instanceof Player ? Level.FINER : Level.FINEST);
    }

}