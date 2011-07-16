package edgruberman.bukkit.simpleregions;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class BlockListener extends org.bukkit.event.block.BlockListener {
    
    private Main main;
    
    public BlockListener(Main plugin) {
        this.main = plugin;
        
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.BLOCK_PLACE, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Main.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to break a block"
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Main.deniedMessage, MessageLevel.SEVERE);
         
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to break a block"
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
                , MessageLevel.FINE
        );
    }
}