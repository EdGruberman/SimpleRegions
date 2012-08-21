package edgruberman.bukkit.simpleregions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.simpleregions.util.BufferedYamlConfiguration;

public class Repository {

    /** minimum time between configuration file saves (milliseconds) */
    public long minSave = 5000;

    private final Plugin plugin;
    private final File defaults;
    private final File worlds;
    private final Map<String, BufferedYamlConfiguration> configuration = new HashMap<String, BufferedYamlConfiguration>();

    Repository(final Plugin plugin, final File defaults, final File worlds) {
        this.plugin = plugin;
        this.defaults = defaults;
        this.worlds = worlds;
    }

    public Region loadDefault(final String world) {
        final BufferedYamlConfiguration config = this.loadConfig(this.defaults, null);

        final String path = (world == null ? "server" : "indices." + world);
        final ConfigurationSection definition = config.getConfigurationSection(path);
        if (definition == null) return null;

        final Region region = new Region(world, null, definition.getStringList("owners"), definition.getStringList("access"));
        region.options.addAll(definition.getStringList("options"));
        region.active = definition.getBoolean("active");
        return region;
    }

    public Collection<Region> loadRegions(final String world) {
        final BufferedYamlConfiguration worldConfig = this.loadConfig(this.worlds, world);
        final Set<Region> regions = new HashSet<Region>();
        final Set<String> names = worldConfig.getKeys(false);
        for (final String name : names) {
            final ConfigurationSection definition = worldConfig.getConfigurationSection(name);

            final Region region = new Region(world, name, definition.getStringList("owners"), definition.getStringList("access"));
            region.options.addAll(definition.getStringList("options"));
            region.active = definition.getBoolean("active");

            final Integer x1 = (definition.isInt("x1") ? definition.getInt("x1", 0) : null);
            final Integer x2 = (definition.isInt("x2") ? definition.getInt("x2", 0) : null);
            final Integer y1 = (definition.isInt("y1") ? definition.getInt("y1", 0) : null);
            final Integer y2 = (definition.isInt("y2") ? definition.getInt("y2", 0) : null);
            final Integer z1 = (definition.isInt("z1") ? definition.getInt("z1", 0) : null);
            final Integer z2 = (definition.isInt("z2") ? definition.getInt("z2", 0) : null);
            region.setCoords(x1, x2, y1, y2, z1, z2);

            if (definition.isString("enter")) region.enter = definition.getString("enter");
            if (definition.isString("exit")) region.exit = definition.getString("exit");

            regions.add(region);
        }

        this.plugin.getLogger().config("Regions loaded for [" + world + "]: " + regions.size());
        return regions;
    }

    public void saveRegion(final Region region, final boolean immediate) {
        final BufferedYamlConfiguration config = this.configuration.get((region.isDefault() ? null : region.world));

        String path = region.name;
        if (region.isDefault())
            if (region.world == null) {
                path = "server";
            } else {
                path = "indices." + region.world;
            }

        final ConfigurationSection section = config.createSection(path);

        section.set("owners", region.owners.members.toArray());
        section.set("access", region.access.members.toArray());
        section.set("options", region.options.toArray());
        section.set("active", region.active);
        if (!region.isDefault()) {
            section.set("enter", region.enter);
            section.set("exit", region.exit);
            section.set("x1", region.getX1());
            section.set("x2", region.getX2());
            section.set("y1", region.getY1());
            section.set("y2", region.getY2());
            section.set("z1", region.getZ1());
            section.set("z2", region.getZ2());
        }

        if (immediate) {
            config.save();
            return;
        }

        config.queueSave();
    }

    public void deleteRegion(final Region region, final boolean immediate) {
        final BufferedYamlConfiguration config = this.configuration.get((region.isDefault() ? null : region.world));

        String path = region.name;
        if (region.isDefault())
            if (region.world == null) {
                path = "server";
            } else {
                path = "indices." + region.world;
            }

        config.set(path, null);

        if (immediate) {
            config.save();
            return;
        }

        config.queueSave();
    }

    public void clear() {
        for (final BufferedYamlConfiguration config : this.configuration.values())
            if (config.isQueued()) config.save();

        this.configuration.clear();
    }

    private BufferedYamlConfiguration loadConfig(final File base, final String world) {
        BufferedYamlConfiguration config = this.configuration.get(world);
        if (config != null) return config;

        final File source = (world == null ? base : new File(base, world + ".yml"));
        try {
            config = new BufferedYamlConfiguration(this.plugin, source, this.minSave);
        } catch (final FileNotFoundException e) {
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to load region configuration file: " + source, e);
        }

        this.configuration.put(world, config);
        return config;
    }

}
