package edgruberman.bukkit.simpleregions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    private Main main;
    
    public EntityListener(Main plugin) {
        this.main = plugin;
        
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PAINTING_BREAK, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PAINTING_PLACE, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onPaintingBreak(PaintingBreakEvent event) {
        if (event.isCancelled()) return;
        
        if (event.getCause().equals(RemoveCause.WORLD)) return;
        
        PaintingBreakByEntityEvent eventByEntity = (PaintingBreakByEntityEvent) event;
        if (!(eventByEntity.getRemover() instanceof Player)) return;
        
        Player player = (Player) eventByEntity.getRemover();
        Block block = event.getPainting().getLocation().getBlock();
        
        if (this.main.isAllowed(player.getName(), player.getWorld().getName()
                , block.getX(), block.getY(), block.getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(player, Main.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + player.getName() + " attempting to break a painting"
                    + " in \"" + player.getWorld().getName() + "\""
                    + " at x:" + block.getX()
                    + " y:" + block.getY()
                    + " z:" + block.getZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onPaintingPlace(PaintingPlaceEvent event) {
        if (event.isCancelled()) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Main.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to place a painting"
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
                , MessageLevel.FINE
        );
    }
}
