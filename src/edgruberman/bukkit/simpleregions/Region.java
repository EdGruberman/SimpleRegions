package edgruberman.bukkit.simpleregions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import edgruberman.bukkit.simplesecurity.AccessControlEntry;
import edgruberman.bukkit.simplesecurity.AccessControlList;
import edgruberman.bukkit.simplesecurity.Principal;

public class Region {
    
    private static final String DEFAULT_ENTER = "Entered \"%1$s\" region."; // 1$ = Region Name
    private static final String DEFAULT_EXIT  = "Exited \"%1$s\" region.";  // 1$ = Region Name
    
    private String worldName = null;
    private String name = null;
    private boolean isActive = true;
    private boolean isDefault = false;
    private boolean isCommitted = true;
    private String enterMessage = null;
    private String exitMessage = null;
    
    private Integer x1 = null, x2 = null, y1 = null, y2 = null, z1 = null, z2 = null;
    private Integer minX = null, maxX = null, minY = null, maxY = null, minZ = null, maxZ = null;
    
    private AccessControlList acl = new AccessControlList();
    
    protected Region(String worldName, String name, Boolean isActive
            , int x1, int x2, int y1, int y2, int z1, int z2
            , List<String> owners, List<String> helpers
            , String enterMessage, String exitMessage
    ) {
        
        this.worldName = worldName;
        this.name = name;
        this.isActive = isActive;
        if (helpers != null)
            for (String helper : helpers)
                this.acl.addPermission(helper);
        
        if (this.name == null) {
            this.isDefault = true;
        } else {
            if (owners != null)
                for (String owner : owners)
                    this.acl.addOwner(owner);
            
            this.x1 = x1; this.x2 = x2;
            this.y1 = y1; this.y2 = y2;
            this.z1 = z1; this.z2 = z2;
            this.enterMessage = enterMessage;
            this.exitMessage  = exitMessage;
            this.setMinMax();           
        }
    }
    
    protected Region(String worldName, String name) {
        this.worldName = worldName;
        this.name = name;
        
        this.isCommitted = false;
    }
    
    /**
     * Generates string to use as a key in a Map for this region.
     * 
     * @return String to use for key.
     */
    protected String getKey() {
        return Region.formatKey(this.worldName, this.name);
    }
    
    /**
     * Generates a commonly formatted string to reference for use with keys in Maps.
     * 
     * @param worldName
     * @param name
     * @return String to use for key.
     */
    protected static String formatKey(String worldName, String name) {
        name = (name == null ? null : name.toLowerCase());
        return worldName + ":" + name;
    }
    
    private void setMinMax() {
        if (this.x1 != null && this.x2 != null) {
            this.minX = (this.x1 < this.x2) ? this.x1 : this.x2;
            this.maxX = (this.x1 > this.x2) ? this.x1 : this.x2;
        } else {
            if (this.x1 != null) this.minX = this.x1;
            if (this.x2 != null) this.minX = this.x2;
        }
        
        if (this.y1 != null && this.y2 != null) {
            this.minY = (this.y1 < this.y2) ? this.y1 : this.y2;
            this.maxY = (this.y1 > this.y2) ? this.y1 : this.y2;
        } else {
            if (this.y1 != null) this.minY = this.y1;
            if (this.y2 != null) this.minY = this.y2;
        }
        
        if (this.z1 != null && this.z2 != null) {
            this.minZ = (this.z1 < this.z2) ? this.z1 : this.z2;
            this.maxZ = (this.z1 > this.z2) ? this.z1 : this.z2;
        } else {
            if (this.z1 != null) this.minZ = this.z1;
            if (this.z2 != null) this.minZ = this.z2;
        }
    }
    
    /**
     * Determines if region contains the coordinates.
     * 
     * @param worldName
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected boolean contains(String worldName, int x, int y, int z) {
        // A region with a null worldName is a default server region and applies to everywhere in any world.
        if (this.worldName == null) return true;
        
        if (!this.worldName.equals(worldName)) return false;
        
        // A region with a null name is a server region and applies to everywhere in this world.
        if (this.name == null) return true;
        
        return this.contains(x, y, z);
    }
    
    /**
     * Determines if coordinates are within this region.
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate.
     * @return True if coordinates are inside the region; Otherwise false.
     */
    private boolean contains(int x, int y, int z) {
        return !(x < this.minX || x > this.maxX || y < this.minY || y > this.maxY || z < this.minZ || z > this.maxZ);
    }
    
    protected boolean isAllowed(String name) {
        return this.acl.isAllowed(name);
    }
    
    protected boolean isOwner(String name) {
        return this.acl.isOwner(name);
    }
    
    protected boolean isDirectOwner(String name) {
        return this.acl.isDirectOwner(name);
    }
    
    protected boolean isHelper(String name) {
        return this.acl.isAllowed(name);
    }
    
    protected boolean isDirectHelper(String name) {
        for (AccessControlEntry ace : this.acl.getEntries())
            if (ace.getPrincipal().getName().equalsIgnoreCase(name))
                return true;
        
        return false;
    }
    
    protected List<String> getOwners()  {
        List<String> owners = new ArrayList<String>();
        for (Principal owner : this.acl.getOwners())
            owners.add(owner.getDisplayName());
            
        return owners;
    }
    
    protected List<String> getHelpers() {
        List<String> helpers = new ArrayList<String>();
        for (AccessControlEntry ace : this.acl.getEntries())
            helpers.add(ace.getPrincipal().getDisplayName());
        
        return helpers;
    }
    
    protected String getWorldName() { return this.worldName; }
    protected String getName()      { return this.name; }
    protected boolean isDefault()   { return this.isDefault; }
    protected boolean isActive()    { return this.isActive; }
    protected boolean isCommitted() { return this.isCommitted; }
    
    protected Integer getX1() { return this.x1; }
    protected Integer getX2() { return this.x2; }
    protected Integer getY1() { return this.y1; }
    protected Integer getY2() { return this.y2; }
    protected Integer getZ1() { return this.z1; }
    protected Integer getZ2() { return this.z2; }
    
    protected Integer getN() { return this.minX; }
    protected Integer getE() { return this.minZ; }
    protected Integer getS() { return this.maxX; }
    protected Integer getW() { return this.maxZ; }
    protected Integer getU() { return this.maxY; }
    protected Integer getD() { return this.minY; }
    
    protected void setX1(Integer i) {
        this.x1 = i;
        this.setMinMax();
    }
    
    protected void setX2(Integer i) {
        this.x2 = i;
        this.setMinMax();
    }
    
    protected void setY1(Integer i) {
        this.y1 = i;
        this.setMinMax();
    }
    
    protected void setY2(Integer i) {
        this.y2 = i;
        this.setMinMax();
    }
    
    protected void setZ1(Integer i) {
        this.z1 = i;
        this.setMinMax();
    }
    
    protected void setZ2(Integer i) {
        this.z2 = i;
        this.setMinMax();
    }
 
    protected void setN(int i) {
        if (this.x1 != null && this.x2 != null) {
            if (i > this.maxX) {
                this.x1 = i;
                this.x2 = i;
            } else if (this.x1 <= this.x2) { this.x1 = i;
            } else { this.x2 = i;
            }
        } else if (this.x1 == null) { this.x1 = i;
        } else if (this.x2 == null) { this.x2 = i;
        }
        
        this.setMinMax();
    }
 
    protected void setS(int i) {
        if (this.x1 != null && this.x2 != null) {
            if (i < this.minX) {
                this.x1 = i;
                this.x2 = i;
            } else if (this.x1 >= this.x2) { this.x1 = i;
            } else { this.x2 = i;
            }
        } else if (this.x1 == null) { this.x1 = i;
        } else if (this.x2 == null) { this.x2 = i;
        }
        
        this.setMinMax();
    }
    
    protected void setE(int i) {
        if (this.z1 != null && this.z2 != null) {
            if (i > this.maxZ) {
                this.z1 = i;
                this.z2 = i;
            } else if (this.z1 <= this.z2) { this.z1 = i;
            } else { this.z2 = i;
            }
        } else if (this.z1 == null) { this.z1 = i;
        } else if (this.z2 == null) { this.z2 = i;
        }
        
        this.setMinMax();
    }
    
    protected void setW(int i) {
        if (this.z1 != null && this.z2 != null) {
            if (i < this.minZ) {
                this.z1 = i;
                this.z2 = i;
            } else if (this.z1 >= this.z2) { this.z1 = i;
            } else { this.z2 = i;
            }
        } else if (this.z1 == null) { this.z1 = i;
        } else if (this.z2 == null) { this.z2 = i;
        }
        
        this.setMinMax();
    }
    
    protected void setU(int i) {
        if (this.y1 != null && this.y2 != null) {
            if (i < this.minY) {
                this.y1 = i;
                this.y2 = i;
            } else if (this.y1 >= this.y2) { this.y1 = i;
            } else { this.y2 = i;
            }
        } else if (this.y1 == null) { this.y1 = i;
        } else if (this.y2 == null) { this.y2 = i;
        }
        
        this.setMinMax();
    }

    protected void setD(int i) {
        if (this.y1 != null && this.y2 != null) {
            if (i > this.maxY) {
                this.y1 = i;
                this.y2 = i;
            } else if (this.y1 <= this.y2) { this.y1 = i;
            } else { this.y2 = i;
            }
        } else if (this.y1 == null) { this.y1 = i;
        } else if (this.y2 == null) { this.y2 = i;
        }
        
        this.setMinMax();
    }
    
    protected void setName(String name) {
        this.name = name;
    }
        
    /**
     * Deactivated regions will not restrict access.
     * 
     * @param isActive true to activate; false to deactivate.
     */
    protected void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    protected void setCommitted(boolean isCommitted) {
        this.isCommitted = isCommitted;
    }
    
    protected String getEnterMessage() {
        return this.enterMessage;
    }
    
    protected String getEnterFormatted() {
        return (this.enterMessage == null ? String.format(Region.DEFAULT_ENTER, this.name) : this.enterMessage);
    }
    
    protected void setEnterMessage(String message) {
        this.enterMessage = message;
    }
    
    protected String getExitMessage() {
        return this.exitMessage;
    }
    
    protected String getExitFormatted() {
        return (this.exitMessage == null ? String.format(Region.DEFAULT_EXIT, this.name) : this.exitMessage);
    }
    
    protected void setExitMessage(String message) {
        this.exitMessage = message;
    }
    
    protected boolean addOwner(String member) {
        return this.acl.addOwner(member);
    }
    
    protected boolean removeOwner(String member) {
        return this.acl.removeOwner(member);
    }
    
    protected boolean addHelper(String member) {
        return this.acl.addPermission(member);
    }
    
    protected boolean removeHelper(String member) {
        return this.acl.removePermission(member);
    }
    
    /**
     * Generates a human readable representation of this region.
     * 
     * @param format Specifies the visual format to use.
     * @return Pertinent details.
     */
    protected String getDescription(int format) {
        String description = "---- Region: ";
        if (this.getName() == null) {
            description += (this.isDefault ? "\"DEFAULT\"" : "");
        } else {
            description += "\"" + this.getName() + "\"";
        }
        description += " ----";
        if (this.isDefault) description += "\nWorld: " + (this.getWorldName() == null ? "* (SERVER)" : this.getWorldName());
        description += "\nActive: " + this.isActive;
        if (!this.isDefault) description += "\nOwners: " + (this.getOwners().size() == 0 ? "" : join(this.getOwners(), " "));
        description += "\nHelpers: " + (this.getHelpers().size() == 0 ? "" : join(this.getHelpers(), " "));
        if (!this.isDefault) description += "\n" + this.getCoordinateReference(format);
        if (!this.isCommitted) description += "\n **** UNCOMMITTED ****";
        
        return description;
    }
    
    /**
     * Generates a textual representation of the volumetric size of this region.</br>
     * </br>
     * Example: 100x * 50y * 128z = 640,000 blocks
     * 
     * @return String representation.
     */
    protected String getSize() {
        Integer sizeX = null;
        if (this.getN() != null && this.getS() != null)
            sizeX = Math.abs(this.getS() - this.getN()) + 1;
        
        Integer sizeY = null;
        if (this.getU() != null && this.getD() != null)
            sizeY = Math.abs(this.getU() - this.getD()) + 1;
        
        Integer sizeZ = null;
        if (this.getE() != null && this.getW() != null)
            sizeZ = Math.abs(this.getW() - this.getE()) + 1;
        
        String size = String.format(
            "%1$sx * %2$sy * %3$sz = %4$s blocks"
            , (sizeX == null ? "?" : sizeX), (sizeY == null ? "?" : sizeY), (sizeZ == null ? "?" : sizeZ)
            , (sizeX != null && sizeY != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeY * sizeZ) : "?")
        );
        
        return size;
    }
    
    /**
     * Generates a textual representation of the two-dimensional area of this
     * region across the x and z axes.</br>
     * </br>
     * Example: 100x * 128z = 12,800 square meters
     * 
     * @return String representation of the area.
     */
    protected String getArea() {
        Integer sizeX = null;
        if (this.getN() != null && this.getS() != null)
            sizeX = Math.abs(this.getS() - this.getN()) + 1;
        
        Integer sizeZ = null;
        if (this.getE() != null && this.getW() != null)
            sizeZ = Math.abs(this.getW() - this.getE()) + 1;
        
        String area = String.format(
                "%1$sx * %2$sz = %3$s square meters"
                , (sizeX == null ? "?" : sizeX), (sizeZ == null ? "?" : sizeZ)
                , (sizeX != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeZ) : "?")
        );
        
        return area;
    }
    
    /**
     * Generates a textual representation of the coordinates that define this region.
     * 
     * @param format Specifies the visual format to use.
     * @return A text-based visual representation.
     */
    private String getCoordinateReference(int format) {
        int lengthMinX = 1;
        if (this.minX != null) lengthMinX = Integer.toString(this.minX).length();
        int lengthMinY = 1;
        if (this.minY != null) lengthMinY = Integer.toString(this.minY).length();
        int lengthMinZ = 1;
        if (this.minZ != null) lengthMinZ = Integer.toString(this.minZ).length();
        int longestMin = lengthMinX;
        longestMin = (lengthMinY > longestMin) ? lengthMinY : longestMin;
        longestMin = (lengthMinZ > longestMin) ? lengthMinZ : longestMin;
        
        int lengthMaxX = 1;
        if (this.maxX != null) lengthMaxX = Integer.toString(this.maxX).length();
        int lengthMaxY = 1;
        if (this.maxY != null) lengthMaxY = Integer.toString(this.maxY).length();
        int lengthMaxZ = 1;
        if (this.maxZ != null) lengthMaxZ = Integer.toString(this.maxZ).length();
        int longestMax = lengthMaxX;
        longestMax = (lengthMaxY > longestMax) ? lengthMaxY : longestMax;
        longestMax = (lengthMaxZ > longestMax) ? lengthMaxZ : longestMax;
        
        int longestX = (lengthMinX > lengthMaxX) ? lengthMinX : lengthMaxX;
        int longestY = (lengthMinY > lengthMaxY) ? lengthMinY : lengthMaxY;
     // int longestZ = (lengthMinZ > lengthMaxZ) ? lengthMinZ : lengthMaxZ;
        
        String xN = "x1";
        if (this.x1 != null && this.x2 != null) {
            xN = (this.x1 <= this.x2 ? "x1" : "x2");
        } else {
            if (this.x2 != null) xN = "x2";
        }
        String xS = (xN.equals("x1") ? "x2" : "x1");
        
        String yD = "y1";
        if (this.y1 != null && this.y2 != null) {
            yD = (this.y1 <= this.y2 ? "y1" : "y2");
        } else {
            if (this.y2 != null) yD = "y2";
        }
        String yU = (yD.equals("y1") ? "y2" : "y1");
        
        String zE = "z1";
        if (this.z1 != null && this.z2 != null) {
            zE = (this.z1 <= this.z2 ? "z1" : "z2");
        } else {
            if (this.z2 != null) zE = "z2";
        }
        String zW = (zE.equals("z1") ? "z2" : "z1");
        
        
        switch (format) {
            case 1:
                // Example:
                // [N] x1:   100 <= X <=  200 :x2 [S]
                // [D] y1:     0 <= Y <=  127 :y2 [U]
                // [E] z2: -2000 <= Z <= 2000 :z1 [W]
                return String.format(
                      "[N] %1$s: %2$#"  + longestMin + "s <= X <= %3$#"  + longestMax + "s :%4$s [S]\n"
                    + "[D] %5$s: %6$#"  + longestMin + "s <= Y <= %7$#"  + longestMax + "s :%8$s [U]\n"
                    + "[E] %9$s: %10$#" + longestMin + "s <= Z <= %11$#" + longestMax + "s :%12$s [W]"
                    , xN, (this.minX == null ? "?" : this.minX), xS, (this.maxX == null ? "?" : this.maxX)
                    , yD, (this.minY == null ? "?" : this.minY), yU, (this.maxY == null ? "?" : this.maxY)
                    , zE, (this.minZ == null ? "?" : this.minZ), zW, (this.maxZ == null ? "?" : this.maxZ)
                );
               
            case 2:
                // Example:
                //          x2: -1550  [N]             y2: 127 [U] 
                // z1: 1700         [W]   [E]  z2: 650             
                //          x1: -1500  [S]             y1:   0 [D] 
                return String.format(
                    repeat(" ", 5 + lengthMaxZ)
                    + "%1$s: %2$#" + longestX + "s  [N]"
                    + repeat(" ", 10 + lengthMinZ) + "%3$s: %4$#" + longestY + "s [U]" + "\n"
                    + "%5$s: %6$#s" + repeat(" ", 5 + longestX - 1)
                    + "[W]   [E]  %7$s: %8$#s" + repeat(" ", 9 + longestY) + "\n"
                    + repeat(" ", 5 + lengthMaxZ) + "%9$s: %10$#" + longestX + "s  [S]"
                    + repeat(" ", 10 + lengthMinZ) + "%11$s: %12$#" + longestY + "s [D]"
                    , xN, (this.minX == null ? "?" : this.minX), yU, (this.maxY == null ? "?" : this.maxY)
                    , zW, (this.maxZ == null ? "?" : this.maxZ), zE, (this.minZ == null ? "?" : this.minZ)
                    , xS, (this.maxX == null ? "?" : this.maxX), yD, (this.minY == null ? "?" : this.minY)
                );
                
            case 3:
                // Example:
                // x1:-100 [N]   y2:127 [U]   z2:2000 [W]   z1:-2000 [E]
                // x2: 200 [S]   y1:  0 [D]
                return String.format(
                      "%1$s: %2$#" + longestX + "s [N]   %3$s: %4$#" + longestY + "s [U]   "
                    + "%5$s: %6$s [W]   %7$s: %8$s [E]\n"
                    + "%9$s: %10$#" + longestX + "s [S]   %11$s: %12$#" + longestY + "s [D]"
                    , xN, (this.minX == null ? "?" : this.minX), yU, (this.maxY == null ? "?" : this.maxY)
                    , zW, (this.maxZ == null ? "?" : this.maxZ), zE, (this.minZ == null ? "?" : this.minZ)
                    , xS, (this.maxX == null ? "?" : this.maxX), yD, (this.minY == null ? "?" : this.minY)
                );
        }
        
        return null;
    }
    
    /**
     * Generate a repeating string of s, n times.
     * 
     * @param s String to repeat.
     * @param n Number of times to repeat s.
     * @return String of s repeated n times.
     */
    private static String repeat(String s, int n) { return new String(new char[n]).replace("\0", s); }
    
    /**
     * Combine all the elements of a list together with a delimiter between each.
     * 
     * @param list List of elements to join.
     * @param delim Delimiter to place between each element.
     * @return String combined with all elements and delimiters.
     */
    private static String join(List<String> list, String delim) {
        if (list.isEmpty()) return "";
     
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());
        
        return sb.toString();
    }
}