package edgruberman.bukkit.simpleregions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class PaintingGuard extends EntityListener {
    
    PaintingGuard(final Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PAINTING_BREAK, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PAINTING_PLACE, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onPaintingBreak(final PaintingBreakEvent event) {
        if (event.isCancelled()) return;
        
        if (!event.getCause().equals(RemoveCause.ENTITY)) return;
        
        PaintingBreakByEntityEvent eventByEntity = (PaintingBreakByEntityEvent) event;
        if (!(eventByEntity.getRemover() instanceof Player)) return;
        
        Player player = (Player) eventByEntity.getRemover();
        if (Main.isAllowed(player, event.getPainting().getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(player, Region.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + player.getName() + " attempting to break a painting"
                    + " in \"" + player.getWorld().getName() + "\""
                    + " at x:" + event.getPainting().getLocation().getBlockX()
                    + " y:" + event.getPainting().getLocation().getBlockY()
                    + " z:" + event.getPainting().getLocation().getBlockZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onPaintingPlace(final PaintingPlaceEvent event) {
        if (event.isCancelled()) return;
        
        if (Main.isAllowed(event.getPlayer(), event.getBlock().getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);
        
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