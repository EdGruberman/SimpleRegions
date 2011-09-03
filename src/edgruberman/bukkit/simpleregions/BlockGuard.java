package edgruberman.bukkit.simpleregions;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class BlockGuard extends BlockListener {
    
    BlockGuard(final Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.BLOCK_PLACE, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        if (Main.isAllowed(event.getPlayer(), event.getBlock().getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to break a " + event.getBlock().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        if (Main.isAllowed(event.getPlayer(), event.getBlock().getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);
         
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to place a " + event.getBlock().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
                , MessageLevel.FINE
        );
    }
}