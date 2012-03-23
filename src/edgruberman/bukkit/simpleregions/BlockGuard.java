package edgruberman.bukkit.simpleregions;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class BlockGuard implements Listener {

    private final Plugin plugin;

    BlockGuard(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) return;

        if (Main.isAllowed(event.getPlayer(), event.getBlock().getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + event.getPlayer().getName() + " attempting to break a " + event.getBlock().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
        );
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        if (Main.isAllowed(event.getPlayer(), event.getBlock().getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + event.getPlayer().getName() + " attempting to place a " + event.getBlock().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
        );
    }

}
