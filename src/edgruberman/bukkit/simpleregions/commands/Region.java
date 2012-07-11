package edgruberman.bukkit.simpleregions.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.simpleregions.Catalog;
import edgruberman.bukkit.simpleregions.Permission;

public final class Region extends Command implements CommandExecutor {

    public static final String NAME = "region";
    public static final Set<String> OWNER_ALLOWED = new HashSet<String>(Arrays.asList(RegionActive.NAME, RegionAccess.NAME, RegionOwner.NAME));

    Catalog catalog;
    Map<CommandSender, edgruberman.bukkit.simpleregions.Region> working = new HashMap<CommandSender, edgruberman.bukkit.simpleregions.Region>();

    public Region(final JavaPlugin plugin, final Catalog catalog) {
        super(plugin, Region.NAME, Permission.REGION);
        this.catalog = catalog;

        this.setExecutorOf(this);

        final RegionCurrent regionCurrent = new RegionCurrent(this);
        this.registerAction(new RegionTarget(this, regionCurrent));

        final RegionSet regionSet = new RegionSet(this);
        this.registerAction(regionSet);
        this.registerAction(new RegionActive(this, regionSet));
        this.registerAction(new RegionCreate(this, regionSet));
        this.registerAction(new RegionDelete(this, regionSet));

        this.registerAction(regionCurrent, true);
        this.registerAction(new RegionDetail(this));
        this.registerAction(new RegionSize(this));
        this.registerAction(new RegionAccess(this));
        this.registerAction(new RegionOwner(this));
        this.registerAction(new RegionMessage(this));
        this.registerAction(new RegionName(this));
        this.registerAction(new RegionDefine(this));
        this.registerAction(new RegionReload(this));
    }

    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        final Context context = super.parse(this, sender, command, label, args);

        boolean owner = false;
        final edgruberman.bukkit.simpleregions.Region region = this.parseRegion(context);
        if (context.player != null && context.action != null && region != null && region.isOwner(context.player)) {
            // if region owner, then allow certain actions
            // TODO integrate this "better"
            if (Region.OWNER_ALLOWED.contains(context.action.name)) owner = true;
        }

        if (!owner) {
            // Standard access checks
            if (!this.isAllowed(context.sender)) {
                context.respond("You are not allowed to use the " + context.label + " command.", MessageLevel.RIGHTS);
                return true;
            }

            if (context.action == null) {
                context.respond("Unrecognized action for the " + context.label + " command.", MessageLevel.WARNING);
                return true;
            }

            if (!context.action.isAllowed(context.sender)) {
                context.respond("You are not allowed to use the " + context.action.name + " action of the " + context.label + " command.", MessageLevel.RIGHTS);
                return true;
            }

        }

        context.action.execute(context);

        return true;
    }

    @Override
    void parseAction(final Context context) {
        // Check direct action name match in third, second, or first argument. (/<command>[ <World>[ <Region>]]<Action>)
        context.parseAction(2);
    }

    // Command Syntax: /region[[ <World>] <Region>][ <Action>][ <Parameters>]
    static World parseWorld(final Context context) {
        // Assume player's world if not specified.
        if (context.actionIndex != 2) {
            if (context.player == null) return null;

            return context.player.getWorld();
        }

        final String name = context.arguments.get(context.actionIndex - 2);
        if (name.equals(edgruberman.bukkit.simpleregions.Region.SERVER_DEFAULT)) return null;

        return context.owner.plugin.getServer().getWorld(name);
    }

    // Command Syntax: /region[[ <World>] <Region>][ <Action>][ <Parameters>]
    edgruberman.bukkit.simpleregions.Region parseRegion(final Context context) {
        if (context.actionIndex <= 0) {
            // Use current working region if specified
            if (this.working.containsKey(context.sender)) return this.working.get(context.sender);

            // Assume player's current region if player is in only one
            if (context.player == null) return null;

            final Set<edgruberman.bukkit.simpleregions.Region> regions = this.catalog.getRegions(context.player.getLocation());
            if (regions.size() != 1) return null;

            return regions.iterator().next();
        }

        final World world = Region.parseWorld(context);
        final String name = context.arguments.get(context.actionIndex - 1);

        if (name.equals(edgruberman.bukkit.simpleregions.Region.NAME_DEFAULT))
            return this.catalog.getDefault(world);

        if (world == null) return null;

        return this.catalog.worlds.get(world.getName()).regions.get(name.toLowerCase());
    }

    void sendIfOnline(final String name, final MessageLevel level, final String message) {
        if (Region.getExactPlayer(name) == null) return;

        MessageManager.of(this.plugin).send(Region.getExactPlayer(name), message, level);
    }

    /**
     * Returns player only if it is a full and case insensitive name match.
     *
     * @param name name of player
     * @return player that matches name
     */
    static Player getExactPlayer(final String name) {
        final Player player = Bukkit.getServer().getPlayer(name);
        if (player == null) return null;

        if (!player.getName().equalsIgnoreCase(name)) return null;

        return player;
    }

    static boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch(final Exception e) {
            return false;
        }
    }

}
