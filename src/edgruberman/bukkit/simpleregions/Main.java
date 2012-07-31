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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
import edgruberman.bukkit.simpleregions.commands.RegionEnter;
import edgruberman.bukkit.simpleregions.commands.RegionExit;
import edgruberman.bukkit.simpleregions.commands.RegionInfo;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersGrant;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersReset;
import edgruberman.bukkit.simpleregions.commands.RegionOwnersRevoke;
import edgruberman.bukkit.simpleregions.commands.RegionSet;
import edgruberman.bukkit.simpleregions.commands.RegionTarget;
import edgruberman.bukkit.simpleregions.commands.RegionUnset;
import edgruberman.bukkit.simpleregions.commands.Reload;

public final class Main extends JavaPlugin implements RegionRepository {

    /**
     * Base path, relative to plugin data folder, to look for world specific
     * configuration overrides in. Sub-folder is used to avoid conflicts
     * between world names and configuration file names. (e.g. a world named
     * config.)
     */
    private static final String WORLD_SPECIFICS = "Worlds";

    public static Messenger messenger;

    private final Map<World, ConfigurationFile> configuration = new HashMap<World, ConfigurationFile>();
    private Catalog catalog = null;

    @Override
    public void onEnable() {
        this.reloadConfig();

        Main.messenger = Messenger.load(this, "messages");
        this.catalog = new Catalog(this, this);
        this.loadServerDefault(this.catalog, this.getConfig().getConfigurationSection("DEFAULT"));
        new Guard(this.catalog);
        new BoundaryAlerter(this.catalog);

        this.getCommand("simpleregions:reload").setExecutor(new Reload(this));
        this.getCommand("simpleregions:region.current").setExecutor(new RegionCurrent(this.catalog));
        this.getCommand("simpleregions:region.target").setExecutor(new RegionTarget(this.catalog));
        this.getCommand("simpleregions:region.set").setExecutor(new RegionSet(this.catalog));
        this.getCommand("simpleregions:region.unset").setExecutor(new RegionUnset(this.catalog));
        this.getCommand("simpleregions:region.info").setExecutor(new RegionInfo(this.catalog));
        this.getCommand("simpleregions:region.activate").setExecutor(new RegionActivate(this.catalog));
        this.getCommand("simpleregions:region.deactivate").setExecutor(new RegionDeactivate(this.catalog));
        this.getCommand("simpleregions:region.owners.grant").setExecutor(new RegionOwnersGrant(this.catalog));
        this.getCommand("simpleregions:region.owners.revoke").setExecutor(new RegionOwnersRevoke(this.catalog));
        this.getCommand("simpleregions:region.owners.reset").setExecutor(new RegionOwnersReset(this.catalog));
        this.getCommand("simpleregions:region.access.grant").setExecutor(new RegionAccessGrant(this.catalog));
        this.getCommand("simpleregions:region.access.revoke").setExecutor(new RegionAccessRevoke(this.catalog));
        this.getCommand("simpleregions:region.access.reset").setExecutor(new RegionAccessReset(this.catalog));
        this.getCommand("simpleregions:region.enter").setExecutor(new RegionEnter(this.catalog));
        this.getCommand("simpleregions:region.exit").setExecutor(new RegionExit(this.catalog));
        this.getCommand("simpleregions:region.create").setExecutor(new RegionCreate(this.catalog));
        this.getCommand("simpleregions:region.define").setExecutor(new RegionDefine(this.catalog));
        this.getCommand("simpleregions:region.delete").setExecutor(new RegionDelete(this.catalog));
    }

    @Override
    public void onDisable() {
        for (final ConfigurationFile config : this.configuration.values())
            if (config.isSaveQueued()) config.save();

        Main.messenger = null;
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.configuration.get(null) == null) this.configuration.put(null, new ConfigurationFile(this, 10));
        return this.configuration.get(null).getConfig();
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        if (this.configuration.get(null) != null) this.configuration.get(null).load();
        this.setLogLevel(this.getConfig().getString("logLevel"));
    }

    public void loadServerDefault(final Catalog catalog, final ConfigurationSection definition) {
        if (definition == null) {
            catalog.serverDefault = null;
            return;
        }

        final Region region = new Region(new HashSet<String>(definition.getStringList("access")));
        region.setActive(definition.getBoolean("active"));
        catalog.serverDefault = region;
    }

    @Override
    public void saveDefaultConfig() {
        this.extractConfig("config.yml", false);
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

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

    @Override
    public Collection<Region> loadRegions(final World world) {
        if (world == null)
            throw new IllegalArgumentException("Unable to load regions; World can not be null");

        if (!this.configuration.containsKey(world))
            this.configuration.put(world, new ConfigurationFile(this, Main.WORLD_SPECIFICS + "/" + world.getName() + ".yml"));

        final Set<Region> regions = new HashSet<Region>();
        final FileConfiguration worldConfig = this.configuration.get(world).getConfig();
        final Set<String> names = worldConfig.getKeys(false);
        for (final String name : names) {
            final ConfigurationSection config = worldConfig.getConfigurationSection(name);

            final Region region = new Region(
                      world
                    , (name.equals(Region.NAME_DEFAULT) ? null : name)
                    , new HashSet<String>(config.getStringList("owners"))
                    , new HashSet<String>(config.getStringList("access"))
            );

            final Integer x1 = (config.isInt("x1") ? config.getInt("x1", 0) : null);
            final Integer x2 = (config.isInt("x2") ? config.getInt("x2", 0) : null);
            final Integer y1 = (config.isInt("y1") ? config.getInt("y1", 0) : null);
            final Integer y2 = (config.isInt("y2") ? config.getInt("y2", 0) : null);
            final Integer z1 = (config.isInt("z1") ? config.getInt("z1", 0) : null);
            final Integer z2 = (config.isInt("z2") ? config.getInt("z2", 0) : null);
            region.setCoords(x1, x2, y1, y2, z1, z2);

            if (config.isString("enter")) region.enter.setFormat(config.getString("enter"));
            if (config.isString("exit")) region.exit.setFormat(config.getString("exit"));

            region.setActive(config.getBoolean("active"));

            regions.add(region);
        }

        this.getLogger().config("Regions defined for [" + world.getName() + "]: " + regions.size());
        return regions;
    }

    @Override
    public void saveRegion(final Region region, final boolean immediate) {
        final ConfigurationFile file = this.configuration.get(region.world);
        final String regionName = (region.getName() == null ? Region.NAME_DEFAULT : region.getName());
        final ConfigurationSection config = file.getConfig().createSection(regionName);

        config.set("active", region.isActive());
        config.set("access", region.access);
        if (!region.isDefault()) {
            config.set("owners", region.owners);
            config.set("enter", (region.enter == null ? null : region.enter.getFormat()));
            config.set("exit", (region.exit == null ? null : region.exit.getFormat()));
            config.set("x1", region.getX1());
            config.set("x2", region.getX2());
            config.set("y1", region.getY1());
            config.set("y2", region.getY2());
            config.set("z1", region.getZ1());
            config.set("z2", region.getZ2());
        }

        file.save(immediate);
    }

    @Override
    public void deleteRegion(final Region region, final boolean immediate) {
        final ConfigurationFile file = this.configuration.get(region.world);
        final String regionName = (region.getName() == null ? Region.NAME_DEFAULT : region.getName());
        file.getConfig().set(regionName, null);
        file.save(immediate);
    }

    public static String formatNames(final Collection<Region> regions, final Player access) {
        String formatted = "";
        for (final Region region : regions) {
            if (formatted.length() > 0) formatted += Main.messenger.getFormat("regionName.+delimiter");
            formatted += String.format((region.hasAccess(access) ? Main.messenger.getFormat("regionName.+hasAccess") : Main.messenger.getFormat("regionName.+noAccess")), region.formatName());
        }
        return formatted;
    }

}
