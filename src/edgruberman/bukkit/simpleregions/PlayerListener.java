package edgruberman.bukkit.simpleregions;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        
        if (!Main.MONITORED_ITEMS.contains(event.getPlayer().getItemInHand().getType())) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
        
        Main.messageManager.log(MessageLevel.FINE
                , "Cancelled " + event.getPlayer().getName() + " attempting to interact"
                + " with a " + event.getPlayer().getItemInHand().getType().name()
                + " in \"" + event.getPlayer().getWorld().getName() + "\""
                + " at x:" + event.getClickedBlock().getX()
                + " y:" + event.getClickedBlock().getY()
                + " z:" + event.getClickedBlock().getZ()
        );
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Update regions' online players lists to include this player if appropriate.
        this.main.addOnlinePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Update regions' online players lists to remove this player if appropriate.
        this.main.removeOnlinePlayer(event.getPlayer());
    }
}