package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

/**
 * case insensitive membership list; inheritance can only be determined for online players
 * due to the way Permissions are calculated by Bukkit
 */
public class AccessList {

    /** permission names (players or groups) */
    public final Set<String> members = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    public AccessList() {}

    public AccessList(final Collection<String> names) {
        this.members.addAll(names);
    }

    /** explicit membership */
    public boolean add(final String name) {
        return this.members.add(name);
    }

    /** explicit membership */
    public boolean remove(final String name) {
        return this.members.remove(name);
    }

    public void clear() {
        this.members.clear();
    }

    /** explicit membership; case insensitive */
    public boolean contains(final String name) {
        return this.members.contains(name);
    }

    /** indirect membership, inherited permission must be explicitly set (no defaulting of ops) */
    public boolean inherits(final CommandSender player) {
        if (this.contains(player.getName())) return true;

        // permissions must be explicitly set to true to avoid default ops getting access due to permission that doesn't exist
        for (final String name : this.members)
            if (player.isPermissionSet(name) && player.hasPermission(name))
                return true;

        return false;
    }

    public List<String> inheritance(final CommandSender player) {
        final List<String> paths = new ArrayList<String>();
        for (final String name : this.members)
            if (player.hasPermission(name))
                paths.addAll(this.inheritance(player, Bukkit.getPluginManager().getPermission(name), Arrays.asList(name)));

        return paths;
    }

    private List<String> inheritance(final CommandSender player, final Permission permission, final List<String> paths) {
        if (player.isPermissionSet(permission)) return paths;

        final String parent = paths.get(paths.size() - 1);
        for (final Map.Entry<String, Boolean> child : permission.getChildren().entrySet()) {
            if (!child.getValue() || !player.hasPermission(child.getKey())) continue;

            if (!paths.get(paths.size() - 1).equals(parent)) paths.add(parent);
            paths.set(paths.size() - 1, parent + ">" + permission.getName());
            this.inheritance(player, permission, paths);
        }

        return paths;
    }

    @Override
    public String toString() {
        return this.members.toString();
    }

}
