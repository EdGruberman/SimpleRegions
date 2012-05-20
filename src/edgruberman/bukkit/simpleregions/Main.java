package edgruberman.bukkit.simpleregions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {

    /**
     * Prefix for all permissions used in this plugin.
     */
    public static final String PERMISSION_PREFIX = "simpleregions";

    /**
     * Base path, relative to plugin data folder, to look for world specific
     * configuration overrides in. Sub-folder is used to avoid conflicts
     * between world names and configuration file names. (e.g. a world named
     * config.)
     */
    private static final String WORLD_SPECIFICS = "Worlds";

    public static MessageManager messageManager;
    public static ConfigurationFile configurationFile;

    public static Plugin plugin;

    static AccountManager security;

    private static Map<World, ConfigurationFile> worldFiles = new HashMap<World, ConfigurationFile>();

    @Override
    public void onEnable() {
        Main.configurationFile = new ConfigurationFile(this, 10);
        Main.configurationFile.load();
        this.setLoggingLevel();

        Main.messageManager = new MessageManager(this);

        Main.plugin = this;
        Main.security = AccountManager.get();
        Main.configure(false);

        new IndexPublisher(this);
        new BlockGuard(this);
        new InteractionGuard(this);
        new PaintingGuard(this);
        new BoundaryAlerter(this);

        new edgruberman.bukkit.simpleregions.commands.Region(this);
    }

    @Override
    public void onDisable() {
        if (Main.configurationFile.isSaveQueued()) Main.configurationFile.save();
    }

    private void setLoggingLevel() {
        final String name = Main.configurationFile.getConfig().getString("logLevel", "INFO");
        Level level = MessageLevel.parse(name);
        if (level == null) level = Level.INFO;

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Logging level set to: " + this.getLogger().getLevel());
    }

    public static void configure(final boolean reload) {
        FileConfiguration config = Main.configurationFile.getConfig();
        if (reload) config = Main.configurationFile.load();

        Region.deniedMessage = config.getString("deniedMessage", null);
        Main.plugin.getLogger().log(Level.CONFIG, "Denied Message: " + Region.deniedMessage);

        Main.loadServerDefault();

        Index.worlds.clear();
        for (final World world : Main.plugin.getServer().getWorlds())
            new Index(world);
    }

    public static boolean loadServerDefault() {
        final ConfigurationSection entry = Main.configurationFile.getConfig().getConfigurationSection("DEFAULT");
        if (entry == null) {
            Index.serverDefault = null;
            return true;
        }

        final Region region = new Region(entry.getBoolean("active", false), new HashSet<String>(entry.getStringList("access")));
        if (!Index.add(region)) {
            Main.plugin.getLogger().log(Level.WARNING, "Unable to add " + Region.SERVER_DEFAULT_DISPLAY + " " + Region.NAME_DEFAULT_DISLAY + " region.");
            return false;
        }

        Main.plugin.getLogger().log(Level.FINEST, region.describe(3));
        return true;
    }

    public static int loadRegions(final World world) {
        if (!Main.worldFiles.containsKey(world)) {
            Main.worldFiles.put(world, new ConfigurationFile(Main.plugin, Main.WORLD_SPECIFICS + "/" + world.getName() + ".yml"));
        } else {
            Main.worldFiles.get(world).load();
        }

        final FileConfiguration cfg = Main.worldFiles.get(world).getConfig();
        final Set<String> regions = cfg.getKeys(false);
        if (regions == null || regions.size() == 0) {
            Main.plugin.getLogger().log(Level.CONFIG, "No regions defined for [" + world.getName() + "]");
            return 0;
        }

        for (final String key : regions) {
            final Region region = new Region(
                      world
                    , (key.equals(Region.NAME_DEFAULT) ? null : key)
                    , cfg.getConfigurationSection(key).getBoolean("active", false)
                    , cfg.getConfigurationSection(key).getInt("x1", 0)
                    , cfg.getConfigurationSection(key).getInt("x2", 0)
                    , cfg.getConfigurationSection(key).getInt("y1", 0)
                    , cfg.getConfigurationSection(key).getInt("y2", 0)
                    , cfg.getConfigurationSection(key).getInt("z1", 0)
                    , cfg.getConfigurationSection(key).getInt("z2", 0)
                    , new HashSet<String>(cfg.getConfigurationSection(key).getStringList("owners"))
                    , new HashSet<String>(cfg.getConfigurationSection(key).getStringList("access"))
                    , cfg.getConfigurationSection(key).getString("enter", null)
                    , cfg.getConfigurationSection(key).getString("exit", null)
            );
            if (Index.add(region)) {
                Main.plugin.getLogger().log(Level.FINEST, region.describe(3));
            } else {
                Main.plugin.getLogger().log(Level.WARNING, "Unable to add [" + region.getWorld().getName() + "] " + region.getDisplayName() + " region.");
            }
        }

        final int count = Index.worlds.get(world).regions.size();
        Main.plugin.getLogger().log(Level.CONFIG, "Loaded " + count + " regions for [" + world.getName() + "]");
        return count;
    }

    public static void saveRegion(final Region region, final boolean immediate) {
        ConfigurationFile file = Main.configurationFile;
        if (region.getWorld() != null)
            file = Main.worldFiles.get(region.getWorld());

        final FileConfiguration cfg = file.getConfig();
        final String regionName = (region.getName() == null ? Region.NAME_DEFAULT : region.getName());

        cfg.set(regionName + ".active", region.isActive());
        cfg.set(regionName + ".access", region.access.formatAllowed());
        if (!region.isDefault()) {
            cfg.set(regionName + ".owners", region.access.formatOwners());
            cfg.set(regionName + ".enter", (region.enter == null ? null : region.enter.getFormat()));
            cfg.set(regionName + ".exit", (region.exit == null ? null : region.exit.getFormat()));
            cfg.set(regionName + ".x1", region.getX1());
            cfg.set(regionName + ".x2", region.getX2());
            cfg.set(regionName + ".y1", region.getY1());
            cfg.set(regionName + ".y2", region.getY2());
            cfg.set(regionName + ".z1", region.getZ1());
            cfg.set(regionName + ".z2", region.getZ2());
        }

        file.save(immediate);
    }

    public static void deleteRegion(final Region region, final boolean immediate) {
        ConfigurationFile file = Main.configurationFile;
        if (region.getWorld() != null)
            file = Main.worldFiles.get(region.getWorld());

        final String regionName = (region.getName() == null ? Region.NAME_DEFAULT : region.getName());
        file.getConfig().set(regionName, null);

        file.save(immediate);
    }

    /**
     * Determine if player is allowed to manipulate the target location based
     * on region configuration.<br>
     * Access is determined by:<br>
     *     = true if any active region applies and allows access to the player<br>
     *     = true if no active region applies and the world default region allows access to the player<br>
     *     = true if no active region applies and no world default region applies and the server default region allows access to the player<br>
     *     = false otherwise
     *
     * @param player player to check access for
     * @param target block to determine if player has access to
     * @return true if player has access, otherwise false
     */
    public static boolean isAllowed(final Player player, final Location target) {
        return Main.isAllowed(player.getName(), target);
    }

    /**
     * Determine if player is allowed to manipulate the target location based
     * on region configuration.<br>
     * Access is determined by:<br>
     *     = true if any active region applies and allows access to the player<br>
     *     = true if no active region applies and the world default region allows access to the player<br>
     *     = true if no active region applies and no world default region applies and the server default region allows access to the player<br>
     *     = false otherwise
     *
     * @param name player name to check access for
     * @param target block to determine if player has access to
     * @return true if player has access, otherwise false
     */
    public static boolean isAllowed(final String name, final Location target) {
        boolean found = false;

        // Check loaded regions.
        for (final Region region : Index.getChunkRegions(target))
            if (region.contains(target)) {
                if (region.access.isAllowed(name)) return true;
                found = true;
            }

        // If at least one loaded region was found, that indicates all applicable regions would have been found in loaded.
        if (found) return false;

        // Check all regions only if chunk is not loaded at target.
        // Slightly redundant in checking loaded regions again, but this should be a rare edge case.
        if (!target.getWorld().isChunkLoaded(target.getBlockX() >> 4, target.getBlockZ() >> 4)) {
            for (final Region region : Index.worlds.get(target.getWorld()).regions.values())
                if (region.isActive() && region.contains(target)) {
                    if (region.access.isAllowed(name)) return true;
                    found = true;
                }

            // If we found at least one applicable unloaded region, do not check default regions.
            if (found) return false;
        }

        // Check world default region only if no other regions apply.
        final Region worldDefault = Index.worlds.get(target.getWorld()).worldDefault;
        if (worldDefault != null && worldDefault.isActive())
            return Index.worlds.get(target.getWorld()).worldDefault.access.isAllowed(name);

        // Check server default region only if no other regions apply and there is no world default region.
        if (Index.serverDefault != null && Index.serverDefault.isActive())
            return Index.serverDefault.access.isAllowed(name);

        return false;
    }

}
