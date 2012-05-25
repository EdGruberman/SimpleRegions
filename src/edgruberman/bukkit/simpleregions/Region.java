package edgruberman.bukkit.simpleregions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.securables.SimpleAccessControlList;
import edgruberman.bukkit.simpleregions.util.CachingRectangularCuboid;
import edgruberman.bukkit.simpleregions.util.CaseInsensitiveString;
import edgruberman.bukkit.simpleregions.util.ChunkCoordinates;
import edgruberman.bukkit.simpleregions.util.FormattedString;

public final class Region extends CachingRectangularCuboid {

    private static final String DEFAULT_ENTER_FORMAT = "Entered region: %1$s"; // 1 = Region Name
    private static final String DEFAULT_EXIT_FORMAT  = "Exited region: %1$s";  // 1 = Region Name

    public static final String SERVER_DEFAULT = "SERVER";
    public static final String NAME_DEFAULT = "DEFAULT";
    public static final String SERVER_DEFAULT_DISPLAY = "(SERVER)";
    public static final String NAME_DEFAULT_DISLAY = "(DEFAULT)";

    public final World world;
    public Index worldIndex = null;
    public FormattedString enter = null;
    public FormattedString exit = null;
    public SimpleAccessControlList access;

    private CaseInsensitiveString name;
    private boolean active = false;

    public Region(final AccountManager accountManager, final World world, final String name) {
        this.world = world;
        this.name = new CaseInsensitiveString(name);
        this.access = new SimpleAccessControlList(accountManager);
        this.enter = new FormattedString(Region.DEFAULT_ENTER_FORMAT, this.name);
        this.exit = new FormattedString(Region.DEFAULT_EXIT_FORMAT, this.name);
    }

    public Region(final AccountManager accountManager, final World world, final String name, final Collection<String> owners, final Collection<String> access) {
        this(accountManager, world, name);
        if (owners != null) for (final String o : owners) this.access.addOwner(o);
        if (access != null) for (final String a : access) this.access.grant(a);
    }

    /**
     * Create a world default region
     */
    public Region(final AccountManager accountManager, final Collection<String> access, final World world) {
        this(accountManager, world, null, null, access);
    }

    /**
     * Create a server default region
     */
    public Region(final AccountManager accountManager, final Collection<String> access) {
        this(accountManager, access, null);
    }

    public void setCoords(final Integer x1, final Integer x2, final Integer y1, final Integer y2, final Integer z1, final Integer z2) {
        this.x1 = x1; this.x2 = x2;
        this.y1 = y1; this.y2 = y2;
        this.z1 = z1; this.z2 = z2;
        this.update();
    }

    public String getName() {
        return this.name.toString();
    }

    public String getDisplayName() {
        if (this.isDefault()) return Region.NAME_DEFAULT_DISLAY;

        String display = this.getName();
        if (display.contains(" ")) display = "\"" + display + "\"";

        return display;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isDefault() {
        return this.getName() == null;
    }

    @Override
    public boolean isDefined() {
        return this.isDefault() || super.isDefined();
    }

    public boolean setActive(final boolean active) {
        if (!this.isDefined()) return false;

        if (this.active == active) return true;

        this.active = active;
        this.refreshIndex();
        return true;
    }

    public boolean setName(final String name) {
        if (this.worldIndex.regions.containsKey(name.toLowerCase())) return false;

        this.name = new CaseInsensitiveString(name);
        this.enter.setArgs(this.name.toString());
        this.exit.setArgs(this.name.toString());

        this.refreshIndex();
        return true;
    }

    private void refreshIndex() {
        if (this.worldIndex == null) return;

        this.worldIndex.refresh(this);
    }

    @Override
    protected void update() {
        super.update();
        this.refreshIndex();
    }

    public boolean contains(final Location loc) {
        return this.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Calculate list of chunks that contain at least one block of the region
     *
     * @param region region to calculate chunks for
     * @return all chunk coordinates that contain a part of region
     */
    public List<ChunkCoordinates> chunks() {
        if (!this.isDefined()) return Collections.<ChunkCoordinates>emptyList();

        final List<ChunkCoordinates> coords = new ArrayList<ChunkCoordinates>();
        for (int x = this.getMinChunkX(); x <= this.getMaxChunkX(); x++)
            for (int z = this.getMinChunkZ(); z <= this.getMaxChunkZ(); z++)
                coords.add(new ChunkCoordinates(x, z));

        return coords;
    }

    @Override
    public String toString() {
        return "[" + this.world.getName() + "] (x:" + this.getX1() + ",y:" + this.getY1() + ",z:" + this.getZ1() + ") - (x:" + this.getX2() + ",y:" + this.getY2() + ",z:" + this.getZ2() + ")";
    }

    public String describe() {
        return this.describe(null);
    }

    /**
     * Generates a human readable representation of a region.
     *
     * @param format Specifies the visual format to use.
     * @return Pertinent details.
     */
    public String describe(final Integer format) {
        String description = "---- Region: " + this.getDisplayName() + " ----";
        description += "\nWorld: " + (this.world == null ? Region.SERVER_DEFAULT_DISPLAY : this.world.getName());
        description += "\nActive: " + this.active;
        if (!this.isDefault()) description += "\nOwners: " + (this.access.getOwners().size() == 0 ? "" : Region.join(this.access.formatOwners(), " "));
        description += "\nAccess: " + (this.access.getEntries().size() == 0 ? "" : Region.join(this.access.formatAllowed(), " "));
        if (!this.isDefault()) description += "\n" + this.describeCoordinates(format);

        return description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.world == null) ? 0 : this.world.getName().hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;

        if (this.getClass() != other.getClass()) return false;
        final Region that = (Region) other;

        // World must match.
        if (this.world == null && that.world != null) {
            return false;
        } else if (!this.world.equals(that.world)) {
            return false;
        }

        // Name must match.
        if (this.name == null && that.name != null) {
            return false;
        } else if (!this.name.equals(that.name)) {
            return false;
        }

        return true;
    }

    /**
     * Combine all the elements of a list together with a delimiter between each.
     *
     * @param list List of elements to join.
     * @param delim Delimiter to place between each element.
     * @return String combined with all elements and delimiters.
     */
    private static String join(final List<String> list, final String delim) {
        if (list.isEmpty()) return "";

        final StringBuilder sb = new StringBuilder();
        for (final String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());

        return sb.toString();
    }

}
