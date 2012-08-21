package edgruberman.bukkit.simpleregions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.simpleregions.util.BlockCuboid;

/** protected area */
public final class Region extends BlockCuboid {

    public final String world;
    public final String name;
    public boolean active = false;
    public String enter = null;
    public String exit = null;
    public final AccessList owners = new AccessList();
    public final AccessList access = new AccessList();
    public final Set<String> options = new HashSet<String>();

    public Region(final String world, final String name, final Collection<String> owners, final Collection<String> access) {
        this.world = world;
        this.name = name;
        this.owners.members.addAll(owners);
        this.access.members.addAll(access);
    }

    /** clone an existing region with a new name */
    public Region(final Region original, final String newName) {
        this(original.world, newName, original.owners.members, original.access.members);
        this.active = original.active;
        this.enter = original.enter;
        this.exit = original.exit;
        this.setCoords(original.x1, original.x2, original.y1, original.y2, original.z1, original.z2);
        this.options.addAll(original.options);
    }

    public void setCoords(final Integer x1, final Integer x2, final Integer y1, final Integer y2, final Integer z1, final Integer z2) {
        this.x1 = x1; this.x2 = x2;
        this.y1 = y1; this.y2 = y2;
        this.z1 = z1; this.z2 = z2;
        this.refresh();
    }

    public boolean hasAccess(final CommandSender player) {
        return this.owners.inherits(player) || this.access.inherits(player);
    }

    public boolean isDefault() {
        return this.name == null;
    }

    @Override
    public boolean isDefined() {
        return this.isDefault() || super.isDefined();
    }

    @Override
    public String toString() {
        return "[" + this.world + "] \"" + this.name + "\" " + super.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.world == null) ? 0 : this.world.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final Region other = (Region) obj;
        if (this.name == null) {
            if (other.name != null) return false;
        } else if (!this.name.equals(other.name)) return false;
        if (this.world == null) {
            if (other.world != null) return false;
        } else if (!this.world.equals(other.world)) return false;
        return true;
    }

}
