package edgruberman.bukkit.simpleregions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

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
    
    private static Plugin plugin;
    private static Map<World, ConfigurationFile> worldFiles = new HashMap<World, ConfigurationFile>();
    
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configurationFile = new ConfigurationFile(this, 10);
        Main.plugin = this;
    }
    
    public void onEnable() {
        Main.loadConfiguration(false);
        
        new IndexPublisher(this);
        new BlockGuard(this);
        new InteractionGuard(this);
        new PaintingGuard(this);
        new BoundaryAlerter(this);
        
        new edgruberman.bukkit.simpleregions.commands.Region(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    public static void loadConfiguration(final boolean reload) {
        if (reload) Main.configurationFile.load();
        
        Region.deniedMessage = Main.configurationFile.getConfiguration().getString("deniedMessage", null);
        Main.messageManager.log("Denied Message: " + Region.deniedMessage, MessageLevel.CONFIG);
        
        Main.loadServerDefault();
        
        Index.worlds.clear();
        for (World world : plugin.getServer().getWorlds())
            new Index(world);
    }
    
    public static boolean loadServerDefault() {
        ConfigurationNode entry = Main.configurationFile.getConfiguration().getNode("DEFAULT");
        if (entry == null) {
            Index.serverDefault = null;
            return true;
        }
        
        Region region = new Region(entry.getBoolean("active", false), new HashSet<String>(entry.getStringList("access", null)));
        if (!Index.add(region)) {
            Main.messageManager.log("Unable to add " + Region.SERVER_DEFAULT_DISPLAY + " " + Region.NAME_DEFAULT_DISLAY + " region.", MessageLevel.FINEST);
            return false;
        }
        
        Main.messageManager.log(region.describe(3), MessageLevel.FINEST);
        return true;
    }
    
    public static int loadRegions(final World world) {
        if (!Main.worldFiles.containsKey(world)) {
            Main.worldFiles.put(world, new ConfigurationFile(Main.plugin, WORLD_SPECIFICS + "/" + world.getName() + ".yml"));
        } else {
            Main.worldFiles.get(world).load();
        }
        
        Configuration cfg = Main.worldFiles.get(world).getConfiguration();
        List<String> regions = cfg.getKeys();
        if (regions == null || regions.size() == 0) {
            Main.messageManager.log("No regions defined for [" + world.getName() + "]", MessageLevel.CONFIG);
            return 0;
        }
        
        for (String key : regions) {
            Region region = new Region(
                      world
                    , (key.equals(Region.NAME_DEFAULT) ? null : key)
                    , cfg.getNode(key).getBoolean("active", false)
                    , cfg.getNode(key).getInt("x1", 0)
                    , cfg.getNode(key).getInt("x2", 0)
                    , cfg.getNode(key).getInt("y1", 0)
                    , cfg.getNode(key).getInt("y2", 0)
                    , cfg.getNode(key).getInt("z1", 0)
                    , cfg.getNode(key).getInt("z2", 0)
                    , new HashSet<String>(cfg.getNode(key).getStringList("owners", null))
                    , new HashSet<String>(cfg.getNode(key).getStringList("access", null))
                    , cfg.getNode(key).getString("enter", null)
                    , cfg.getNode(key).getString("exit", null)
            );
            if (Index.add(region))
                Main.messageManager.log(region.describe(3), MessageLevel.FINEST);
        }
        
        int count = Index.worlds.get(world).regions.size();
        Main.messageManager.log("Loaded " + count + " regions for [" + world.getName() + "]", MessageLevel.CONFIG);
        return count;
    }
    
    public void saveRegion(final Region region, final boolean immediate) {
        Configuration cfg = Main.worldFiles.get(region.getWorld()).getConfiguration();
        
        String regionName = (region.getName() == null ? Region.NAME_DEFAULT : region.getName());
        cfg.setProperty(regionName + ".active", region.isActive());
        cfg.setProperty(regionName + ".access", region.accessNames());
        if (!region.isDefault()) {
            cfg.setProperty(regionName + ".owners", region.ownerNames());
            cfg.setProperty(regionName + ".enter", (region.enter == null ? null : region.enter.getFormat()));
            cfg.setProperty(regionName + ".exit", (region.exit == null ? null : region.exit.getFormat()));
            cfg.setProperty(regionName + ".x1", region.getX1());
            cfg.setProperty(regionName + ".x2", region.getX2());
            cfg.setProperty(regionName + ".y1", region.getY1());
            cfg.setProperty(regionName + ".y2", region.getY2());
            cfg.setProperty(regionName + ".z1", region.getZ1());
            cfg.setProperty(regionName + ".z2", region.getZ2());
        }
        
        Main.configurationFile.save(immediate);
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
        boolean found = false;
        
        // Check loaded regions.
        for (Region region : Index.getChunkRegions(target))
            if (region.contains(target)) {
                if (region.access.isAllowed(player)) return true;
                found = true;
            }
        
        // If at least one loaded region was found, that indicates all applicable regions would have been found in loaded.
        if (found) return false;
        
        // Check all regions only if chunk is not loaded at target.
        // Slightly redundant in checking loaded regions again, but this should be a rare edge case.
        if (!target.getWorld().isChunkLoaded(target.getBlockX() >> 4, target.getBlockZ() >> 4)) {
            for (Region region : Index.worlds.get(target.getWorld()).regions.values())
                if (region.isActive() && region.contains(target)) {
                    if (region.access.isAllowed(player)) return true;
                    found = true;
                }
            
            // If we found at least one applicable unloaded region, do not check default regions.
            if (found) return false;
        }
        
        // Check world default region only if no other regions apply.
        Region worldDefault = Index.worlds.get(target.getWorld()).worldDefault;
        if (worldDefault != null && worldDefault.isActive())
            return Index.worlds.get(target.getWorld()).worldDefault.access.isAllowed(player);
        
        // Check server default region only if no other regions apply and there is no world default region.
        if (Index.serverDefault != null && Index.serverDefault.isActive())
            return Index.serverDefault.access.isAllowed(player);
        
        return false;
    }
}