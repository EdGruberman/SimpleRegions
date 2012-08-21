package edgruberman.bukkit.simpleregions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.HandlerList;

import edgruberman.bukkit.simpleregions.commands.RegionAccessGrant;
import edgruberman.bukkit.simpleregions.commands.RegionAccessReset;
import edgruberman.bukkit.simpleregions.commands.RegionAccessRevoke;
import edgruberman.bukkit.simpleregions.commands.RegionActivate;
import edgruberman.bukkit.simpleregions.commands.RegionCreate;
import edgruberman.bukkit.simpleregions.commands.RegionCurrent;
import edgruberman.bukkit.simpleregions.commands.RegionDeactivate;
import edgruberman.bukkit.simpleregions.commands.RegionDefine;
import edgruberman.bukkit.simpleregions.commands.RegionDelete;
import edgruberman.bukkit.simpleregions.commands.RegionDescribe;
import edgruberman.bukkit.simpleregions.commands.RegionEnter;
import edgruberman.bukkit.simpleregions.commands.RegionExit;
import edgruberman.bukkit.simpleregions.commands.RegionOptionAdd;
import edgruberman.bukkit.simpleregions.commands.RegionOptionRemove;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersGrant;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersReset;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersRevoke;
import edgruberman.bukkit.simpleregions.commands.RegionRename;
import edgruberman.bukkit.simpleregions.commands.RegionSet;
import edgruberman.bukkit.simpleregions.commands.RegionTarget;
import edgruberman.bukkit.simpleregions.commands.RegionUnset;
import edgruberman.bukkit.simpleregions.commands.Reload;
import edgruberman.bukkit.simpleregions.messaging.ConfigurationCourier;
import edgruberman.bukkit.simpleregions.messaging.Courier;
import edgruberman.bukkit.simpleregions.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static Courier courier;

    private Catalog catalog = null;

    @Override
    public void onLoad() { this.putConfigMinimum("config.yml", "4.2.0"); }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.Factory.create(this).setBase("messages").build();

        this.extractConfig("defaults.yml", false);
        final Repository repository = new Repository(this, new File(this.getDataFolder(), "defaults.yml"), new File(this.getDataFolder(), "Worlds"));
        this.catalog = new Catalog(this, repository, this.getConfig().getConfigurationSection("options"));
        new Guard(this.catalog, this.parseMaterialList("deniedItems"), this.getConfig().getBoolean("protectFire"));
        final BoundaryAlerter alerter = new BoundaryAlerter(this.catalog);

        this.getCommand("simpleregions:reload").setExecutor(new Reload(this));
        this.getCommand("simpleregions:region.current").setExecutor(new RegionCurrent(this.catalog));
        this.getCommand("simpleregions:region.target").setExecutor(new RegionTarget(this.catalog));
        this.getCommand("simpleregions:region.set").setExecutor(new RegionSet(this.catalog));
        this.getCommand("simpleregions:region.unset").setExecutor(new RegionUnset(this.catalog));
        this.getCommand("simpleregions:region.describe").setExecutor(new RegionDescribe(this.catalog));
        this.getCommand("simpleregions:region.activate").setExecutor(new RegionActivate(this.catalog));
        this.getCommand("simpleregions:region.deactivate").setExecutor(new RegionDeactivate(this.catalog));
        this.getCommand("simpleregions:region.owners.grant").setExecutor(new RegionOwnersGrant(this.catalog));
        this.getCommand("simpleregions:region.owners.revoke").setExecutor(new RegionOwnersRevoke(this.catalog));
        this.getCommand("simpleregions:region.owners.reset").setExecutor(new RegionOwnersReset(this.catalog));
        this.getCommand("simpleregions:region.access.grant").setExecutor(new RegionAccessGrant(this.catalog));
        this.getCommand("simpleregions:region.access.revoke").setExecutor(new RegionAccessRevoke(this.catalog));
        this.getCommand("simpleregions:region.access.reset").setExecutor(new RegionAccessReset(this.catalog));
        this.getCommand("simpleregions:region.enter").setExecutor(new RegionEnter(this.catalog, alerter));
        this.getCommand("simpleregions:region.exit").setExecutor(new RegionExit(this.catalog, alerter));
        this.getCommand("simpleregions:region.create").setExecutor(new RegionCreate(this.catalog));
        this.getCommand("simpleregions:region.define").setExecutor(new RegionDefine(this.catalog));
        this.getCommand("simpleregions:region.delete").setExecutor(new RegionDelete(this.catalog));
        this.getCommand("simpleregions:region.rename").setExecutor(new RegionRename(this.catalog));
        this.getCommand("simpleregions:region.option.add").setExecutor(new RegionOptionAdd(this.catalog));
        this.getCommand("simpleregions:region.option.remove").setExecutor(new RegionOptionRemove(this.catalog));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        this.catalog.clear();
        Main.courier = null;
    }

    private List<Material> parseMaterialList(final String path) {
        final List<Material> materials = new ArrayList<Material>();
        for (final String name : this.getConfig().getStringList(path)) {
            final Material material = Material.getMaterial(name);
            if (material == null) {
                this.getLogger().warning("Unrecognized Material in " + path + ": " + name);
                continue;
            }

            materials.add(material);
        }
        return materials;
    }

}
