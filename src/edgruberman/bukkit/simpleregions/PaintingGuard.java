package edgruberman.bukkit.simpleregions;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class PaintingGuard implements Listener {

    private final Plugin plugin;

    PaintingGuard(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPaintingBreak(final PaintingBreakEvent event) {
        if (event.isCancelled()) return;

        if (!event.getCause().equals(RemoveCause.ENTITY)) return;

        final PaintingBreakByEntityEvent eventByEntity = (PaintingBreakByEntityEvent) event;
        if (!(eventByEntity.getRemover() instanceof Player)) return;

        final Player player = (Player) eventByEntity.getRemover();
        if (Main.isAllowed(player, event.getPainting().getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(player, Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + player.getName() + " attempting to break a painting"
                    + " in \"" + player.getWorld().getName() + "\""
                    + " at x:" + event.getPainting().getLocation().getBlockX()
                    + " y:" + event.getPainting().getLocation().getBlockY()
                    + " z:" + event.getPainting().getLocation().getBlockZ()
        );
    }

    @EventHandler
    public void onPaintingPlace(final PaintingPlaceEvent event) {
        if (event.isCancelled()) return;

        if (Main.isAllowed(event.getPlayer(), event.getBlock().getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + event.getPlayer().getName() + " attempting to place a painting"
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
        );
    }
}