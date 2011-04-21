package edgruberman.bukkit.simpleregions;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import edgruberman.bukkit.simpleregions.MessageManager.MessageLevel;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    private Main main;
    
    public PlayerListener(Main main) {
        this.main = main;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        if (!this.main.isEnabled()) return;

        Block from = event.getFrom().getBlock();
        Block to = event.getTo().getBlock();
        if (from.equals(to)) return;
        
        this.main.checkCrossings(event.getPlayer(), from, to);
    }
    
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        if (!this.main.isEnabled()) return;

        Block from = event.getFrom().getBlock();
        Block to = event.getTo().getBlock();
        if (from.equals(to)) return;
        
        this.main.checkCrossings(event.getPlayer(), from, to);
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        
        if (!this.main.isEnabled()) return;

        if (!Main.MONITORED_ITEMS.contains(event.getPlayer().getItemInHand().getType())) return;
        
        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage.length() != 0)
            Main.messageManager.send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.main.isEnabled()) return;
        
        // Update regions' online players lists to include this player if appropriate.
        this.main.addOnlinePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!this.main.isEnabled()) return;
        
        // Update regions' online players lists to remove this player if appropriate.
        this.main.removeOnlinePlayer(event.getPlayer());
    }
}