package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

final class Guard implements Listener {

    private final Catalog catalog;
    private final List<Integer> targetFace = new ArrayList<Integer>();
    private final boolean protectFire;

    public Guard(final Catalog catalog, final List<Material> targetFace, final boolean protectFire) {
        this.catalog = catalog;
        for (final Material material : targetFace) this.targetFace.add(material.getId());
        this.protectFire = protectFire;
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
        this.catalog.plugin.getLogger().log(Level.FINE,
                "Cancelled {0} attempting to break {1} at {2}"
                , new Object[] { event.getPlayer().getName(), event.getBlock().getType().name(), target });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Location target = event.getBlock().getLocation();
        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().log(Level.FINE,
                "Cancelled {0} attempting to place {1} {2}"
                , new Object[] { event.getPlayer().getName(), event.getItemInHand().getType(), target });
    }

    /** cancel protected fire block hits and items used that affect the block based on the face clicked */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final boolean leftClickCheckFire = this.protectFire && event.getAction() == Action.LEFT_CLICK_BLOCK;
        if (!leftClickCheckFire && !this.rightClickTargetFace(event)) return;

        final Location target = event.getClickedBlock().getLocation();
        target.add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());

        if (leftClickCheckFire && event.getClickedBlock().getWorld().getBlockTypeIdAt(target) != Material.FIRE.getId()) return;

        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().log(Level.FINE,
                "Cancelled {0} attempting to interact with {1} at {2} on {3}"
                , new Object[] { event.getPlayer().getName(), ( event.getItem() == null ? "hand" : event.getItem().getType().name() ), target, event.getBlockFace() });
    }

    private boolean rightClickTargetFace(final PlayerInteractEvent interact) {
        if (interact.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        return this.targetFace.contains(( interact.getItem() == null ? null : interact.getItem().getTypeId() ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) return;

        final Player player = (Player) event.getRemover();
        final Location target = event.getEntity().getLocation();
        if (this.catalog.isAllowed(player, target)) return;

        event.setCancelled(true);
        this.tellDenied(player, target);
        this.catalog.plugin.getLogger().log(Level.FINE,
                "Cancelled {0} attempting to break a painting at {1}", new Object[] { player.getName(), target });
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(final HangingPlaceEvent event) {
        final Location target = event.getBlock().getLocation();
        target.add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());
        if (this.catalog.isAllowed(event.getPlayer(), target)) return;

        event.setCancelled(true);
        this.tellDenied(event.getPlayer(), target);
        this.catalog.plugin.getLogger().log(Level.FINE,
                "Cancelled {0} attempting to place a painting at {1}", new Object[] { event.getPlayer().getName(), target });
    }

}
