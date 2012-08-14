package edgruberman.bukkit.simpleregions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

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
import edgruberman.bukkit.simpleregions.commands.RegionOwnersGrant;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersReset;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersRevoke;
import edgruberman.bukkit.simpleregions.commands.RegionRename;
import edgruberman.bukkit.simpleregions.commands.RegionSet;
import edgruberman.bukkit.simpleregions.commands.RegionTarget;
import edgruberman.bukkit.simpleregions.commands.RegionUnset;
import edgruberman.bukkit.simpleregions.commands.Reload;
import edgruberman.bukkit.simpleregions.messaging.couriers.ConfigurationCourier;
import edgruberman.bukkit.simpleregions.messaging.couriers.TimestampedConfigurationCourier;
import edgruberman.bukkit.simpleregions.util.Version;

public final class Main extends JavaPlugin {

    private static final Version MINIMUM_CONFIGURATION = new Version("4.0.0");

    public static ConfigurationCourier courier;

    private Catalog catalog = null;

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = new TimestampedConfigurationCourier(this, "messages");

        this.extractConfig("defaults.yml", false);
        final Repository repository = new Repository(this, new File(this.getDataFolder(), "defaults.yml"), new File(this.getDataFolder(), "Worlds"));
        this.catalog = new Catalog(this, repository);
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
    }

    @Override
    public void onDisable() {
        this.catalog.clear();
        Main.courier = null;
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"));

        final Version version = new Version(this.getConfig().isSet("version") ? this.getConfig().getString("version") : null);
        if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) return;

        this.archiveConfig("config.yml", version);
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void saveDefaultConfig() {
        this.extractConfig("config.yml", false);
    }

    private void archiveConfig(final String resource, final Version version) {
        final String backupName = "%1$s - Archive version %2$s - %3$tY%3$tm%3$tdT%3$tH%3$tM%3$tS.yml";
        final File backup = new File(this.getDataFolder(), String.format(backupName, resource.replaceAll("(?i)\\.yml$", ""), version, new Date()));
        final File existing = new File(this.getDataFolder(), resource);

        if (!existing.renameTo(backup))
            throw new IllegalStateException("Unable to archive configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");

        this.getLogger().warning("Archived configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
    }

    private void extractConfig(final String resource, final boolean replace) {
        final Charset source = Charset.forName("UTF-8");
        final Charset target = Charset.defaultCharset();
        if (target.equals(source)) {
            super.saveResource(resource, replace);
            return;
        }

        final File config = new File(this.getDataFolder(), resource);
        if (config.exists()) return;

        final char[] cbuf = new char[1024]; int read;
        try {
            final Reader in = new BufferedReader(new InputStreamReader(this.getResource(resource), source));
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config), target));
            while((read = in.read(cbuf)) > 0) out.write(cbuf, 0, read);
            out.close(); in.close();

        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
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
