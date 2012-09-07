package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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

import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

final class Guard implements Listener {

    private static final List<Integer> BUCKETS = new ArrayList<Integer>(Arrays.asList(
              Material.BUCKET.getId()
            , Material.WATER_BUCKET.getId()
            , Material.LAVA_BUCKET.getId()
    ));

    private static final List<Integer> SIGNS = new ArrayList<Integer>(Arrays.asList(
            Material.SIGN_POST.getId()
          , Material.WALL_SIGN.getId()
  ));

    private final Catalog catalog;
    private final List<Integer> deniedItems = new ArrayList<Integer>();
    private final boolean protectFire;

    public Guard(final Catalog catalog, final List<Material> deniedItems, final boolean protectFire) {
        this.catalog = catalog;
        for (final Material material : deniedItems) this.deniedItems.add(material.getId());
        this.protectFire = protectFire;
        catalog.plugin.getServer().getPluginManager().registerEvents(this, catalog.plugin);
    }

    private void tellDenied(final Player player, final Location target) {
        final String current = RegionExecutor.formatNames(this.catalog.getRegions(target), player);
        Main.courier.send(player, "denied", current);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Location target = event.getBlock().getLocation();
        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().fine(
                "Cancelled " + event.getPlayer().getName() + " attempting to break " + event.getBlock().getType().name() + " " + this.formatLocation(target));

        if (!Guard.SIGNS.contains(event.getBlock().getTypeId())) return;

        // refresh signs to keep text showing
        final Sign state = (Sign) event.getBlock().getState();
        state.update();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Location target = event.getBlock().getLocation();
        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().fine(
                "Cancelled " + event.getPlayer().getName() + " attempting to place " + event.getItemInHand().getType().name() + " " + this.formatLocation(target));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final ItemStack inHand = event.getItem();
        if (inHand != null && !(this.protectFire && this.leftClickFire(event)) && !this.deniedItems.contains(inHand.getTypeId())) return;

        final Location target = event.getClickedBlock().getLocation();
        if (inHand != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && Guard.BUCKETS.contains(inHand.getTypeId()))
            target.add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());

        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().fine(
                "Cancelled " + event.getPlayer().getName() + " attempting to interact with " + inHand.getType().name() + " " + this.formatLocation(target));
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
