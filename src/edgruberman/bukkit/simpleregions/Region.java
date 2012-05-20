package edgruberman.bukkit.simpleregions;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import edgruberman.bukkit.CachingRectangularCuboid;
import edgruberman.bukkit.accesscontrol.securables.SimpleAccessControlList;
import edgruberman.java.CaseInsensitiveString;
import edgruberman.java.FormattedString;

public final class Region extends CachingRectangularCuboid {

    private static final String DEFAULT_ENTER_FORMAT = "Entered region: %1$s"; // 1 = Region Name
    private static final String DEFAULT_EXIT_FORMAT  = "Exited region: %1$s";  // 1 = Region Name
    private static final String DEFAULT_DENIED_MESSAGE = "No regions grant you access here.";

    public static final String SERVER_DEFAULT = "SERVER";
    public static final String NAME_DEFAULT = "DEFAULT";
    public static final String SERVER_DEFAULT_DISPLAY = "(SERVER)";
    public static final String NAME_DEFAULT_DISLAY = "(DEFAULT)";

    static String deniedMessage = Region.DEFAULT_DENIED_MESSAGE;

    private World world;
    private CaseInsensitiveString name;
    private boolean active = false;
    public FormattedString enter = null;
    public FormattedString exit = null;
    public SimpleAccessControlList access = new SimpleAccessControlList(Main.security);

    Region(final World world, final String name, final boolean active
            , final Integer x1, final Integer x2, final Integer y1, final Integer y2, final Integer z1, final Integer z2
            , final Set<String> owners, final Set<String> access
            , final String enterFormat, final String exitFormat
    ) {
        this(world, name);

        super.setX1(x1); super.setX2(x2);
        super.setY1(y1); super.setY2(y2);
        super.setZ1(z1); super.setZ2(z2);

        if (active && (this.isDefined() || this.getName() == null))
            this.active = active;

        if (owners != null)
            for (final String owner : owners)
                this.access.addOwner(owner);

        if (access != null)
            for (final String a : access)
                this.access.grant(a);

        if (enterFormat != null) this.enter.setFormat(enterFormat);
        if (exitFormat != null) this.exit.setFormat(exitFormat);
    }

    /**
     * Create incomplete region instance. Used to define a new region.
     *
     * @param name region name
     */
    public Region(final World world, final String name) {
        super();

        this.world = world;
        this.name = new CaseInsensitiveString(name);

        this.enter = new FormattedString(Region.DEFAULT_ENTER_FORMAT, this.name);
        this.exit = new FormattedString(Region.DEFAULT_EXIT_FORMAT, this.name);
    }

    /**
     * Create a server default region.
     *
     * @param active
     * @param access
     */
    Region(final boolean active, final Set<String> access) {
        this(null, null, active, null, null, null, null, null, null, null, access, null, null);
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(final World world) {
        this.world = world;
    }

    public String getName() {
        return this.name.toString();
    }

    public String getDisplayName() {
        if (this.getName() == null) return Region.NAME_DEFAULT_DISLAY;

        String display = this.getName();
        if (display.contains(" ")) display = "\"" + display + "\"";

        return display;
    }

    public boolean contains(final Location loc) {
        return this.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean isDefault() {
        return (this.getWorld() != null ? this.equals(Index.worlds.get(this.getWorld()).worldDefault) : this.equals(Index.serverDefault));
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean setActive(final boolean active) {
        // Only set active to true if all coordinates supplied or it is a default region
        if (!this.isDefined() && this.getName() != null) return false;

        if (this.active == active) return true;

        this.active = active;

        if (Index.exists(this)) {
            Index.refresh(this);
        } else {
            Index.add(this);
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + this.getWorld().getName() + "] (x:" + this.getX1() + ",y:" + this.getY1() + ",z:" + this.getZ1() + ") - (x:" + this.getX2() + ",y:" + this.getY2() + ",z:" + this.getZ2() + ")";
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
        description += "\nWorld: " + (this.getWorld() == null ? Region.SERVER_DEFAULT_DISPLAY : this.getWorld().getName());
        description += "\nActive: " + this.active;
        if (!this.isDefault()) description += "\nOwners: " + (this.access.getOwners().size() == 0 ? "" : Region.join(this.access.formatOwners(), " "));
        description += "\nAccess: " + (this.access.getEntries().size() == 0 ? "" : Region.join(this.access.formatAllowed(), " "));
        if (!this.isDefault()) description += "\n" + this.describeCoordinates(format);

        return description;
    }

    public boolean setName(final String name) {
        if (Index.exists(new Region(this.world, name))) return false;

        this.name = new CaseInsensitiveString(name);
        this.enter.setArgs(this.name.toString());
        this.exit.setArgs(this.name.toString());

        return Index.refresh(this);
    }

    @Override
    public void setD(final int y) {
        super.setD(y);
        Index.refresh(this);
    }

    @Override
    public void setE(final int z) {
        super.setE(z);
        Index.refresh(this);
    }

    @Override
    public void setN(final int x) {
        super.setN(x);
        Index.refresh(this);
    }

    @Override
    public void setS(final int x) {
        super.setS(x);
        Index.refresh(this);
    }

    @Override
    public void setU(final int y) {
        super.setU(y);
        Index.refresh(this);
    }

    @Override
    public void setW(final int z) {
        super.setW(z);
        Index.refresh(this);
    }

    @Override
    public void setY1(final Integer y) {
        super.setY1(y);
        Index.refresh(this);
    }

    @Override
    public void setZ1(final Integer z) {
        super.setZ1(z);
        Index.refresh(this);
    }

    @Override
    public void setX1(final Integer x) {
        super.setX1(x);
        Index.refresh(this);
    }

    @Override
    public void setX2(final Integer x) {
        super.setX2(x);
        Index.refresh(this);
    }

    @Override
    public void setY2(final Integer y) {
        super.setY2(y);
        Index.refresh(this);
    }

    @Override
    public void setZ2(final Integer z) {
        super.setZ2(z);
        Index.refresh(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getWorld() == null) ? 0 : this.getWorld().getName().hashCode());
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
        if (this.getWorld() == null && that.getWorld() != null) {
            return false;
        } else if (!this.getWorld().equals(that.getWorld())) {
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