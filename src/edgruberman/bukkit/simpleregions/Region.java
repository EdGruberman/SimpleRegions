package edgruberman.bukkit.simpleregions;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import edgruberman.accesscontrol.AccessControlEntry;
import edgruberman.accesscontrol.Principal;
import edgruberman.bukkit.CachingRectangularCuboid;
import edgruberman.bukkit.accesscontrol.AccountManager;
import edgruberman.bukkit.accesscontrol.SimpleAccess;
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
    public SimpleAccess access = new SimpleAccess();
    
    Region(final World world, final String name, final boolean active
            , final Integer x1, final Integer x2, final Integer y1, final Integer y2, final Integer z1, final Integer z2
            , final Set<String> owners, final Set<String> access
            , final String enterFormat, final String exitFormat
    ) {
        this(world, name);
        
        this.setX1(x1); this.setX2(x2);
        this.setY1(y1); this.setY2(y2);
        this.setZ1(z1); this.setZ2(z2);
        
        if (active && (this.isDefined() || this.getName() == null))
            this.active = active;
        
        if (owners != null)
            for (String owner : owners)
                this.access.addOwner(owner);
        
        if (access != null)
            for (String a : access)
                this.access.grant(a);
        
        this.enter = new FormattedString(Region.DEFAULT_ENTER_FORMAT, this.name);
        if (enterFormat != null) this.enter.setFormat(enterFormat);
        this.exit = new FormattedString(Region.DEFAULT_EXIT_FORMAT, this.name);
        if (exitFormat != null) this.exit.setFormat(exitFormat);
    }
    
    /**
     * Create incomplete region instance. Used to define a new region.
     * 
     * @param name region name
     */
    Region(final World world, final String name) {
        super();
        
        this.world = world;
        this.name = new CaseInsensitiveString(name);
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
        Index.refresh(this);
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
        if (!this.isDefault()) description += "\nOwners: " + (this.access.getAcl().getOwners().size() == 0 ? "" : join(this.access.formatOwners(), " "));
        description += "\nAccess: " + (this.access.getAcl().getEntries().size() == 0 ? "" : join(this.access.formatAllowed(), " "));
        if (!this.isDefault()) description += "\n" + this.describeCoordinates(format);
        
        return description;
    }
    
    boolean setName(final String name) {
        if (!Index.isUnique(new Region(this.world, name))) return false;
        
        this.name = new CaseInsensitiveString(name);
        this.enter.setArgs(this.name.toString());
        this.exit.setArgs(this.name.toString());
        
        return Index.refresh(this);
    }
    
    public boolean isDirectOwner(final String name) {
        for (Principal owner : this.access.getAcl().getOwners())
            if (AccountManager.formatName(owner).equalsIgnoreCase(name))
                return true;
        
        return false;
    }
    
    public boolean isDirectAccess(final String name) {
        for (AccessControlEntry ace : this.access.getAcl().getEntries())
            if (AccountManager.formatName(ace.getPrincipal()).equalsIgnoreCase(name))
                return true;
        
        return false;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getWorld() == null) ? 0 : this.getWorld().getName().hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        
        if (this.getClass() != other.getClass()) return false;
        Region that = (Region) other;
        
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
     
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());
        
        return sb.toString();
    }
}