package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.simpleregions.MessageManager.MessageLevel;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    /**
     * Items who uses are cancelled if a player is interacting with a block in a region they do not have access to.
     */
    public static final Set<Material> MONITORED_ITEMS = new HashSet<Material>(Arrays.asList(new Material[] {
          Material.BUCKET
        , Material.WATER_BUCKET
        , Material.LAVA_BUCKET
        , Material.FLINT_AND_STEEL
    }));
    
    private final int DEFAULT_SAVE_MINIMUM = 300; // Duration in seconds to wait since last update before saving configuration file again.
    
    private final String DEFAULT_LOG_LEVEL       = "RIGHTS";
    private final String DEFAULT_SEND_LEVEL      = "RIGHTS";
    private final String DEFAULT_BROADCAST_LEVEL = "RIGHTS";

    public static MessageManager messageManager = null;
    public static GroupManager groupManager = null;
    
    public static String deniedMessage = null;
    
    public Map<String, Region> uncommittedRegions = new HashMap<String, Region>();

    private Map<String, Region> regions = new HashMap<String, Region>();
    private Map<String, List<String>> groupsConfig = new HashMap<String, List<String>>();
    
    private int saveMinimum;
    private Integer saveTimerID = null;

    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Configuration.load(this);
        
        Main.messageManager.setLogLevel(MessageLevel.parse(      this.getConfiguration().getString("logLevel",       this.DEFAULT_LOG_LEVEL)));
        Main.messageManager.setSendLevel(MessageLevel.parse(     this.getConfiguration().getString("sendLevel",      this.DEFAULT_SEND_LEVEL)));
        Main.messageManager.setBroadcastLevel(MessageLevel.parse(this.getConfiguration().getString("broadcastLevel", this.DEFAULT_BROADCAST_LEVEL)));
        
        Main.groupManager = new GroupManager(this);
        this.loadGroups();
        
        this.saveMinimum = this.getConfiguration().getInt("saveMinimum", this.DEFAULT_SAVE_MINIMUM);
        
        Main.deniedMessage = this.getConfiguration().getString("deniedMessage", null);
        this.loadRegions();

        this.registerEvents();
        
        this.getCommand("region").setExecutor(new CommandManager(this));

        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        this.getCommand("region").setExecutor(null);

        //TODO Unregister listeners when Bukkit supports it.
        
        this.saveRegions(true);
        
        Main.groupManager = null;
        
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
    }
    
    private void registerEvents() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        
        PlayerListener playerListener = new PlayerListener(this);
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE    , playerListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN    , playerListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT    , playerListener, Event.Priority.Monitor, this);
        
        pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal , this);
     
        BlockListener blockListener = new BlockListener(this);
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
    }
    
    public int loadRegions() {
        Map<String, Region> regions = new HashMap<String, Region>();
        String worldName, name;
        
        for (Map.Entry<String, ConfigurationNode> worldEntry : this.getConfiguration().getNodes("regions").entrySet()) {
            worldName = worldEntry.getKey();
            if (worldName.equals("DEFAULT")) {
                // Server Default
                worldName = null;
                this.loadRegion(worldName, null, worldEntry.getValue(), regions);
                continue;
            }
            
            for (Map.Entry<String, ConfigurationNode> regionEntry
                    : this.getConfiguration().getNodes("regions." + worldName).entrySet()) {
                name = regionEntry.getKey();
                if (name.equals("DEFAULT")) name = null; // World Default
                this.loadRegion(worldName, name, regionEntry.getValue(), regions);
            }
        }
        this.regions = regions;
        Main.messageManager.log(MessageLevel.CONFIG, "Loaded " + this.regions.size() + " regions.");
        return this.regions.size();
    }
    
    private void loadRegion(String worldName, String name, ConfigurationNode regionNode, Map<String, Region> regions) {
        if (!this.isRegionUnique(worldName, name, regions)) {
            Main.messageManager.log(MessageLevel.WARNING, "Region \"" + worldName + ":" + name + "\" not loaded; Namespace conflict.");
            return;
        }
        
        Region region = new Region(
                worldName
              , name
              , regionNode.getBoolean("active", true)
              , regionNode.getInt("x1", 0)
              , regionNode.getInt("x2", 0)
              , regionNode.getInt("y1", 0)
              , regionNode.getInt("y2", 0)
              , regionNode.getInt("z1", 0)
              , regionNode.getInt("z2", 0)
              , regionNode.getStringList("owners", null)
              , regionNode.getStringList("helpers", null)
              , regionNode.getString("enter", null)
              , regionNode.getString("exit", null)
              , this
              , Main.groupManager
          );
          region.refreshOnline();
          regions.put(worldName + ":" + region.getName(), region);
          Main.messageManager.log(MessageLevel.FINER, region.getDescription(3));
    }
    
    /**
     * There can be only one!
     * 
     * @param worldName Name of world region is for.
     * @param name Name of region.
     * @param regions Map of regions to test if region is unique in.
     * @return Whether or not region of the same name already exists for the world.
     */
    public boolean isRegionUnique(String worldName, String name, Map<String, Region> regions) {
        if (regions == null) regions = this.regions;
        
        for (Region region : regions.values()) {
            if (region.getWorldName() == worldName && region.getName() == name)
                return false;
        }
        
        return true;
    }
    
    public void addRegion(Region region) {
        this.regions.put(region.getWorldName() + ":" + region.getName(), region);
    }
    
    public void removeRegion(Region region) {
        this.regions.remove(region.getWorldName() + ":" + region.getName());
    }
    
    public void renameRegion(Region region, String name) {
        this.regions.remove(region.getWorldName() + ":" + region.getName());
        region.setName(name);
        this.regions.put(region.getWorldName() + ":" + region.getName(), region);
    }
    
    public void saveRegions(boolean immediate) {
        if (!immediate) {
            if (this.saveTimerID != null) { return; }
            // TODO Create TimerTask to call this.saveRegions(true) once in <saveMinimum> seconds.
            // return;
        }
        
        Map<String, Region> regions = this.regions;

        this.getConfiguration().removeProperty("regions");
        for (Region region : regions.values()) {
            String worldName = region.getWorldName();
            String regionName = (region.getName() == null ? "DEFAULT" : region.getName());
            String nodeName = "regions";
            if (worldName != null) nodeName += "." + worldName;
            nodeName += "." + regionName;
            this.getConfiguration().setProperty(nodeName + ".active", region.isActive());
            this.getConfiguration().setProperty(nodeName + ".helpers", region.getHelpers());
            if (!region.isDefault()) {
                this.getConfiguration().setProperty(nodeName + ".owners", region.getOwners());
                this.getConfiguration().setProperty(nodeName + ".enter", region.getEnterMessage());
                this.getConfiguration().setProperty(nodeName + ".exit", region.getExitMessage());
                this.getConfiguration().setProperty(nodeName + ".x1", region.getX1());
                this.getConfiguration().setProperty(nodeName + ".x2", region.getX2());
                this.getConfiguration().setProperty(nodeName + ".y1", region.getY1());
                this.getConfiguration().setProperty(nodeName + ".y2", region.getY2());
                this.getConfiguration().setProperty(nodeName + ".z1", region.getZ1());
                this.getConfiguration().setProperty(nodeName + ".z2", region.getZ2());
            }
        }
        this.getConfiguration().save();
    }
    
    /**
     * Determines if player has crossed a region boundary and displays a message for the user if configured.</br>
     * (Keep this lean and mean as it gets called on every PLAYER_MOVE event.)
     * 
     * @param player
     * @param from
     * @param to
     */
    public void checkCrossings(Player player, Block from, Block to) {
        for (Region region : this.regions.values()) {
            if (region.isDefault() || !region.isActive()) continue;
            
            boolean isInFrom = region.contains(from.getWorld().getName(), from.getX(), from.getY(), from.getZ());
            boolean isInTo   = region.contains(  to.getWorld().getName(),   to.getX(),   to.getY(),   to.getZ());
            if (isInFrom == isInTo) continue;
            
            if (region.getExitFormatted().length() != 0) {
                if (isInFrom) Main.messageManager.send(player, MessageLevel.STATUS, region.getExitFormatted());
            }
            if (region.getEnterFormatted().length() != 0)
                if (isInTo)   Main.messageManager.send(player, MessageLevel.STATUS, region.getEnterFormatted());
        }
    }
    
    /**
     * Determines if player is allowed to perform actions on the coordinates.</br>
     * </br>
     * Server Default only applies if no World Default region applies.</br>
     * The World Default region only applies if no other non-default regions apply.</br>
     * This method is called for all BLOCK_BREAK events, all BLOCK_PLACE events, and PLAYER_INTERACT events for monitoredItems.
     * 
     * @param playerName
     * @param worldName
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isAllowed(String playerName, String worldName, int x, int y, int z) {
        // Check if any standard regions allow the player access.
        boolean hasStandard = false;
        for (Region region : this.regions.values()) {
            if (!region.isDefault() && region.isActive() && region.contains(worldName, x, y, z)) {
                hasStandard = true;
                if (region.isHelperOnline(playerName) || region.isOwnerOnline(playerName)) return true;
            }
        }

        // Check the default regions for player access.
        boolean isDefaultAllow = false;
        if (!hasStandard) {
            // Check if the World Default region exists and allows the player access.
            Region worldDefault = this.getRegion(worldName, null);
            if (worldDefault != null) {
                isDefaultAllow = worldDefault.isHelperOnline(playerName);
            } else {
                // Check if the Server Default region exists and allows the player access.
                Region serverDefault = this.getRegion(null, null);
                if (serverDefault != null) isDefaultAllow = serverDefault.isHelperOnline(playerName);
            }
        }
        
        // Default region access only applies if no other regions do.
        return (hasStandard ? false : isDefaultAllow);
    }
    
    //TODO Add flag for returning only regions player is owner of.
    public List<Region> getRegions(Player player) {
        return this.getRegions(player, true);
    }
    
    public List<Region> getRegions(Player player, boolean includeDefault) {
        return this.getRegions(player.getWorld().getName()
                , player.getLocation().getBlockX()
                , player.getLocation().getBlockY()
                , player.getLocation().getBlockZ()
                , includeDefault
        );
    }
    
    public List<Region> getRegions(String worldName, int x, int y, int z, boolean includeDefault) {
        List<Region> containers = new ArrayList<Region>();
        
        for (Region region : this.regions.values()) {
            if (includeDefault || (!includeDefault && !region.isDefault()) ) {
                if (region.contains(worldName, x, y, z)) {
                    containers.add(region);
                }
            }
        }
        
        return containers;
    }
    
    public Region getRegion(String worldName, String name) {
        return this.regions.get(worldName + ":" + name);
    }
    
    public void addOnlinePlayer(Player player) {
        for (Region region : this.regions.values()) {
            region.addOnlinePlayer(player.getName());
        }
    }
    
    public void removeOnlinePlayer(Player player) {
        for (Region region : this.regions.values()) {
            region.removeOnlinePlayer(player.getName());
        }
    }
    
    // TODO Overhaul group management
    private void loadGroups() {
        Map<String, List<String>> groups = new HashMap<String, List<String>>();
        List<String> members = new ArrayList<String>();
        for (String player : this.getConfiguration().getStringList("groups.Players", null)) {
            members.add(player);
        }
        groups.put("Players", members);
        this.groupsConfig = groups;
        Main.messageManager.log(MessageLevel.FINE, "[Players]=" + this.groupsGetMembers("Players"));
    }
    
    public List<String> groupsGetMembers(String group) {
        return this.groupsConfig.get(group);
    }
}
