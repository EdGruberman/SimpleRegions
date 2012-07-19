package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

final class Guard implements Listener {

    /**
     * Items whose uses are cancelled if a player is interacting with
     * a block in a region they do not have access to.
     */
    static final List<Integer> DENIED_ITEMS = new ArrayList<Integer>(Arrays.asList(
              Material.BUCKET.getId()
            , Material.WATER_BUCKET.getId()
            , Material.LAVA_BUCKET.getId()
            , Material.FLINT_AND_STEEL.getId()
    ));

    static final List<Integer> BUCKETS = new ArrayList<Integer>(Arrays.asList(
              Material.BUCKET.getId()
            , Material.WATER_BUCKET.getId()
            , Material.LAVA_BUCKET.getId()
    ));

    private final String denied;
    private final Catalog catalog;

    public Guard(final Catalog catalog, final String denied) {
        this.catalog = catalog;
        this.denied = denied;
        catalog.plugin.getServer().getPluginManager().registerEvents(this, catalog.plugin);
    }

    private void tellDenied(final Player player, final Location target) {
        if (this.denied == null || this.denied.length() == 0) return;

        String current = "";
        for (final Region region : this.catalog.getRegions(target)) {
            if (current.length() > 0) current += ", ";
            current += region.getDisplayName();
        }

        final String denied = String.format(this.denied, current);
        MessageManager.of(this.catalog.plugin).tell(player, denied, MessageLevel.SEVERE, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Location target = event.getBlock().getLocation();
        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().fine(
                "Cancelled " + event.getPlayer().getName() + " attempting to break a " + event.getBlock().getType().name() + " " + this.formatLocation(target));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Location target = event.getBlock().getLocation();
        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().fine(
                "Cancelled " + event.getPlayer().getName() + " attempting to place a " + event.getBlock().getType().name() + " " + this.formatLocation(target));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final ItemStack inHand = event.getPlayer().getItemInHand();
        if (!this.leftClickFire(event) && !Guard.DENIED_ITEMS.contains(inHand.getTypeId())) return;

        final Location target = event.getClickedBlock().getLocation();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Guard.BUCKETS.contains(inHand.getTypeId()))
            target.add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());

        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().fine(
                "Cancelled " + event.getPlayer().getName() + " attempting to interact with a " + inHand.getType().name() + " " + this.formatLocation(target));
    }

    private boolean leftClickFire(final PlayerInteractEvent interaction) {
        if (!interaction.getAction().equals(Action.LEFT_CLICK_BLOCK)) return false;

        final Block clicked = interaction.getClickedBlock();
        return clicked.getWorld().getBlockTypeIdAt(clicked.getX() + interaction.getBlockFace().getModX(), clicked.getY() + interaction.getBlockFace().getModY(), clicked.getZ() + interaction.getBlockFace().getModZ()) == Material.FIRE.getId();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPaintingBreakByEntity(final PaintingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) return;

        final Player player = (Player) event.getRemover();
        final Location target = event.getPainting().getLocation();
        if (this.catalog.isAllowed(player, target)) return;

        event.setCancelled(true);
        this.tellDenied(player, target);
        this.catalog.plugin.getLogger().fine("Cancelled " + player.getName() + " attempting to break a painting " + this.formatLocation(target));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPaintingPlace(final PaintingPlaceEvent event) {
        final Location target = event.getBlock().getLocation();
        target.add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());
        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().fine("Cancelled " + event.getPlayer().getName() + " attempting to place a painting " + this.formatLocation(target));
    }

    private String formatLocation(final Location location) {
        return "[" + location.getWorld().getName() + "] (x:" + location.getBlockX()+ ", y:" + location.getBlockY() + ", z:" + location.getBlockZ() + ")";
    }

}
