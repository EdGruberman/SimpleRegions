package edgruberman.bukkit.simpleregions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    private Main main;
    
    public PlayerListener(Main main) {
        this.main = main;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;

        Block from = event.getFrom().getBlock();
        Block to = event.getTo().getBlock();
        if (from.equals(to)) return;
        
        this.main.checkCrossings(event.getPlayer(), from, to);
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        
        if (
                !(
                        event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                        && event.getClickedBlock().getFace(event.getBlockFace()).getType().equals(Material.FIRE)
                )
                && !Main.getMonitoredItems().contains(event.getPlayer().getItemInHand().getType())
        ) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.getMessageManager().send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
        
        Main.getMessageManager().log(MessageLevel.FINE
                , "Cancelled " + event.getPlayer().getName() + " attempting to interact"
                + " with a " + event.getPlayer().getItemInHand().getType().name()
                + " in \"" + event.getPlayer().getWorld().getName() + "\""
                + " at x:" + event.getClickedBlock().getX()
                + " y:" + event.getClickedBlock().getY()
                + " z:" + event.getClickedBlock().getZ()
        );
    }
    
    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        
        if (!Main.getMonitoredItems().contains(event.getPlayer().getItemInHand().getType())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.getMessageManager().send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
        
        Main.getMessageManager().log(MessageLevel.FINE
                , "Cancelled " + event.getPlayer().getName() + " attempting to interact"
                + " with a " + event.getRightClicked().getClass().getName() + " entity"
                + " in \"" + event.getPlayer().getWorld().getName() + "\""
                + " at x:" + event.getRightClicked().getLocation().getBlockX()
                + " y:" + event.getRightClicked().getLocation().getBlockY()
                + " z:" + event.getRightClicked().getLocation().getBlockZ()
        );
    }
    
    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;
        
        if (!Main.getMonitoredItems().contains(event.getBucket())) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlockClicked().getX(), event.getBlockClicked().getY(), event.getBlockClicked().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.getMessageManager().send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
        
        Main.getMessageManager().log(MessageLevel.FINE
                , "Cancelled " + event.getPlayer().getName() + " attempting to empty"
                + " a " + event.getBucket().name()
                + " in \"" + event.getPlayer().getWorld().getName() + "\""
                + " at x:" + event.getBlockClicked().getX()
                + " y:" + event.getBlockClicked().getY()
                + " z:" + event.getBlockClicked().getZ()
        );
    }
    
    @Override
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (event.isCancelled()) return;
        
        if (!Main.getMonitoredItems().contains(event.getBucket())) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlockClicked().getX(), event.getBlockClicked().getY(), event.getBlockClicked().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.getMessageManager().send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
        
        Main.getMessageManager().log(MessageLevel.FINE
                , "Cancelled " + event.getPlayer().getName() + " attempting to fill"
                + " a " + event.getBucket().name()
                + " in \"" + event.getPlayer().getWorld().getName() + "\""
                + " at x:" + event.getBlockClicked().getX()
                + " y:" + event.getBlockClicked().getY()
                + " z:" + event.getBlockClicked().getZ()
        );
    }
}