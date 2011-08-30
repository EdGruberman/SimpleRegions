package edgruberman.bukkit.simpleregions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    private Main main;
    private Map<Player, Block> last = new HashMap<Player, Block>();
    
    public PlayerListener(final Main plugin) {
        this.main = plugin;
        
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Monitor, plugin);
        
        pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_BUCKET_FILL, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        Block from = this.last.get(event.getPlayer());
        this.last.put(event.getPlayer(), event.getTo().getBlock());
        if (from == null) return;
        
        Block to = event.getTo().getBlock();
        // Players in vehicles seem to drop 1 on y, so bring them back up.
        if (event.getPlayer().isInsideVehicle()) to = to.getRelative(0, 1, 0);
        if (from.equals(to)) return;
        
        this.main.checkCrossings(event.getPlayer(), from, to);
    }
    
    @Override
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        
        if (
                !(
                        event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                        && event.getClickedBlock().getRelative(event.getBlockFace()).getType().equals(Material.FIRE)
                )
                && !Main.MONITORED_ITEMS.contains(event.getPlayer().getItemInHand().getType())
        ) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Main.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to interact"
                    + " with a " + event.getPlayer().getItemInHand().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getClickedBlock().getX()
                    + " y:" + event.getClickedBlock().getY()
                    + " z:" + event.getClickedBlock().getZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        
        if (!Main.MONITORED_ITEMS.contains(event.getPlayer().getItemInHand().getType())) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getRightClicked().getLocation().getBlockX()
                , event.getRightClicked().getLocation().getBlockY()
                , event.getRightClicked().getLocation().getBlockZ())
        ) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Main.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to interact"
                    + " with a " + event.getRightClicked().getClass().getName() + " entity"
                    + " holding a " + event.getPlayer().getItemInHand().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getRightClicked().getLocation().getBlockX()
                    + " y:" + event.getRightClicked().getLocation().getBlockY()
                    + " z:" + event.getRightClicked().getLocation().getBlockZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;
        
        if (!Main.MONITORED_ITEMS.contains(event.getBucket())) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlockClicked().getX(), event.getBlockClicked().getY(), event.getBlockClicked().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Main.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to empty"
                    + " a " + event.getBucket().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlockClicked().getX()
                    + " y:" + event.getBlockClicked().getY()
                    + " z:" + event.getBlockClicked().getZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        if (event.isCancelled()) return;
        
        if (!Main.MONITORED_ITEMS.contains(event.getBucket())) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlockClicked().getX(), event.getBlockClicked().getY(), event.getBlockClicked().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Main.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to fill"
                    + " a " + event.getBucket().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getBlockClicked().getX()
                    + " y:" + event.getBlockClicked().getY()
                    + " z:" + event.getBlockClicked().getZ()
                , MessageLevel.FINE
        );
    }
}