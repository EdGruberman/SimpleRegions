package edgruberman.bukkit.simpleregions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class InteractionGuard implements Listener {

    /**
     * Items whose uses are cancelled if a player is interacting with
     * a block in a region they do not have access to.
     */
    static final Set<Material> DENIED_ITEMS = new HashSet<Material>(Arrays.asList(new Material[] {
          Material.BUCKET
        , Material.WATER_BUCKET
        , Material.LAVA_BUCKET
        , Material.FLINT_AND_STEEL
    }));

    private final Plugin plugin;

    public InteractionGuard(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.isCancelled()) return;

        if (
                !(
                        event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                        && event.getClickedBlock().getRelative(event.getBlockFace()).getType().equals(Material.FIRE)
                )
                && !InteractionGuard.DENIED_ITEMS.contains(event.getPlayer().getItemInHand().getType())
        ) return;

        if (Main.isAllowed(event.getPlayer(), event.getClickedBlock().getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + event.getPlayer().getName() + " attempting to interact"
                    + " with a " + event.getPlayer().getItemInHand().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getClickedBlock().getX()
                    + " y:" + event.getClickedBlock().getY()
                    + " z:" + event.getClickedBlock().getZ()
        );
    }

    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;

        if (!InteractionGuard.DENIED_ITEMS.contains(event.getPlayer().getItemInHand().getType())) return;

        if (Main.isAllowed(event.getPlayer(), event.getRightClicked().getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + event.getPlayer().getName() + " attempting to interact"
                    + " with a " + event.getRightClicked().getClass().getName() + " entity"
                    + " holding a " + event.getPlayer().getItemInHand().getType().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + event.getRightClicked().getLocation().getBlockX()
                    + " y:" + event.getRightClicked().getLocation().getBlockY()
                    + " z:" + event.getRightClicked().getLocation().getBlockZ()
        );
    }

    @EventHandler
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;

        if (!InteractionGuard.DENIED_ITEMS.contains(event.getBucket())) return;

        final Block target = event.getBlockClicked().getRelative(event.getBlockFace());
        if (Main.isAllowed(event.getPlayer(), target.getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + event.getPlayer().getName() + " attempting to empty"
                    + " a " + event.getBucket().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + target.getX()
                    + " y:" + target.getY()
                    + " z:" + target.getZ()
        );
    }

    @EventHandler
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        if (event.isCancelled()) return;

        if (!InteractionGuard.DENIED_ITEMS.contains(event.getBucket())) return;

        final Block target = event.getBlockClicked().getRelative(event.getBlockFace());
        if (Main.isAllowed(event.getPlayer(), target.getLocation())) return;

        event.setCancelled(true);
        if (Region.deniedMessage != null)
            Main.messageManager.send(event.getPlayer(), Region.deniedMessage, MessageLevel.SEVERE);

        this.plugin.getLogger().log(Level.FINE,
                "Cancelled " + event.getPlayer().getName() + " attempting to fill"
                    + " a " + event.getBucket().name()
                    + " in \"" + event.getPlayer().getWorld().getName() + "\""
                    + " at x:" + target.getX()
                    + " y:" + target.getY()
                    + " z:" + target.getZ()
        );
    }

}
