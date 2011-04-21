package edgruberman.bukkit.simpleregions;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import edgruberman.bukkit.simpleregions.MessageManager.MessageLevel;

public class BlockListener extends org.bukkit.event.block.BlockListener {
    
    private Main main;
    
    public BlockListener(Main main) {
        this.main = main;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        if (!this.main.isEnabled()) return;

        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) return;
        
        event.setCancelled(true);
        if (Main.deniedMessage.length() != 0)
            Main.messageManager.send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        if (!this.main.isEnabled()) return;

        if (this.main.isAllowed(event.getPlayer().getName(), event.getPlayer().getWorld().getName()
                , event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) return;
        
        event.setCancelled(true);
         if (Main.deniedMessage.length() != 0)
            Main.messageManager.send(event.getPlayer(), MessageLevel.SEVERE, Main.deniedMessage);
    }
}