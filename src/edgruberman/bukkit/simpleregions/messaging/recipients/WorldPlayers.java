package edgruberman.bukkit.simpleregions.messaging.recipients;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleregions.messaging.Message;
import edgruberman.bukkit.simpleregions.messaging.Recipients;
import edgruberman.bukkit.simpleregions.messaging.messages.Confirmation;

/**
 * players in a world at message delivery time
 *
 * @author EdGruberman (ed@rjump.com)
 * @version 1.0.0
 */
public class WorldPlayers implements Recipients {

    protected final World world;

    public WorldPlayers(final World world) {
        this.world = world;
    }

    @Override
    public Confirmation deliver(final Message message) {
        final List<Player> players = this.world.getPlayers();
        for (final Player player : players)
                player.sendMessage(message.format(player).toString());

        final int count = players.size();
        return new Confirmation(Level.FINE, count
                , "[WORLD%{1}({2})] {0}", message, WorldPlayers.this.world.getName(), count);
    }

}