package edgruberman.bukkit.simpleregions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class InteractionGuard extends PlayerListener {
    
    /**
     * Items who uses are cancelled if a player is interacting with
     * a block in a region they do not have access to.
     */
    static final Set<Material> MONITORED_ITEMS = new HashSet<Material>(Arrays.asList(new Material[] {
          Material.BUCKET
        , Material.WATER_BUCKET
        , Material.LAVA_BUCKET
        , Material.FLINT_AND_STEEL
    }));
    
    public InteractionGuard(final Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        
        pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_BUCKET_FILL, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        
        if (
                !(
                        event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                        && event.getClickedBlock().getRelative(event.getBlockFace()).getType().equals(Material.FIRE)
                )
                && !InteractionGuard.MONITORED_ITEMS.contains(event.getPlayer().getItemInHand().getType())
        ) return;
        
        if (Main.isAllowed(event.getPlayer(), event.getClickedBlock().getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);
        
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
        
        if (!InteractionGuard.MONITORED_ITEMS.contains(event.getPlayer().getItemInHand().getType())) return;
        
        if (Main.isAllowed(event.getPlayer(), event.getRightClicked().getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);
        
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
        
        if (!InteractionGuard.MONITORED_ITEMS.contains(event.getBucket())) return;
        
        Block target = event.getBlockClicked().getRelative(event.getBlockFace());
        if (Main.isAllowed(event.getPlayer(), target.getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to empty"
                    + " a " + event.getBucket().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + target.getX()
                    + " y:" + target.getY()
                    + " z:" + target.getZ()
                , MessageLevel.FINE
        );
    }
    
    @Override
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        if (event.isCancelled()) return;
        
        if (!InteractionGuard.MONITORED_ITEMS.contains(event.getBucket())) return;
        
        Block target = event.getBlockClicked().getRelative(event.getBlockFace());
        if (Main.isAllowed(event.getPlayer(), target.getLocation())) return;
        
        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);
        
        Main.messageManager.log(
                "Cancelled " + event.getPlayer().getName() + " attempting to fill"
                    + " a " + event.getBucket().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + target.getX()
                    + " y:" + target.getY()
                    + " z:" + target.getZ()
                , MessageLevel.FINE
        );
    }
}