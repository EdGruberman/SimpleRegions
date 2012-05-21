package edgruberman.bukkit.simpleregions.util;

import java.text.DecimalFormat;
/**
 * Rectangular cuboid, designed to help performance with frequent
 * attribute reading through up-front caching on property changes.
 */
public class CachingRectangularCuboid {
    
    private static final int DEFAULT_COORDINATE_FORMAT = 3;
    
    private Integer x1 = null, x2 = null, y1 = null, y2 = null, z1 = null, z2 = null;
    private Integer minX = null, maxX = null, minY = null, maxY = null, minZ = null, maxZ = null;
    private Integer minChunkX = null, maxChunkX = null, minChunkZ = null, maxChunkZ = null;
    private boolean defined = false;
    
    /**
     * Determines if specified coordinates are inside. Designed for
     * speed.
     * 
     * @param x X axis coordinate
     * @param y Y axis coordinate
     * @param z Z axis coordinate
     * @return true if coordinates are inside; otherwise false
     * @throws NullPointerException when !isDefined()
     */
    public boolean contains(final int x, final int y, final int z) {
        return !(x < this.minX || x > this.maxX || z < this.minZ || z > this.maxZ || y < this.minY || y > this.maxY);
    }
    
    /**
     * Determines if the chunk at the specified chunk coordinates
     * holds any part of this cuboid.
     * 
     * @param chunkX chunk X axis coordinate
     * @param chunkZ chunk Z axis coordinate
     * @return true if chunk contains any part; otherwise false
     * @throws NullPointerException when !isDefined()
     */
    public boolean within(final int chunkX, final int chunkZ) {
        return chunkX >= this.minChunkX && chunkX <= this.maxChunkX && chunkZ >= this.minChunkZ && chunkZ <= this.maxChunkZ;
    }
    
    public boolean isDefined() {
        return this.defined;
    }
    
    public Integer getX1() { return this.x1; }
    public Integer getX2() { return this.x2; }
    public Integer getY1() { return this.y1; }
    public Integer getY2() { return this.y2; }
    public Integer getZ1() { return this.z1; }
    public Integer getZ2() { return this.z2; }
    
    public Integer getMinChunkX() { return this.minChunkX; }
    public Integer getMaxChunkX() { return this.maxChunkX; }
    public Integer getMinChunkZ() { return this.minChunkZ; }
    public Integer getMaxChunkZ() { return this.maxChunkZ; }
    
    /**
     * Northern most coordinate.
     * 
     * @return minimum X axis value
     */
    public Integer getN() { return this.minX; }
    
    /**
     * Eastern most coordinate.
     * 
     * @return minimum Z axis value
     */
    public Integer getE() { return this.minZ; }
    
    /**
     * Southern most coordinate.
     * 
     * @return maximum X axis value
     */
    public Integer getS() { return this.maxX; }
    
    /**
     * Western most coordinate.
     * 
     * @return maximum Z axis value
     */
    public Integer getW() { return this.maxZ; }
    
    /**
     * Upwards most coordinate.
     * 
     * @return maximum Y axis value
     */
    public Integer getU() { return this.maxY; }
    
    /**
     * Downwards most coordinate.
     * 
     * @return minimum Y axis value
     */
    public Integer getD() { return this.minY; }
    
    public void setX1(final Integer x) {
        this.x1 = x;
        this.update();
    }
    
    public void setX2(final Integer x) {
        this.x2 = x;
        this.update();
    }
    
    public void setY1(final Integer y) {
        this.y1 = y;
        this.update();
    }
    
    public void setY2(final Integer y) {
        this.y2 = y;
        this.update();
    }
    
    public void setZ1(final Integer z) {
        this.z1 = z;
        this.update();
    }
    
    public void setZ2(final Integer z) {
        this.z2 = z;
        this.update();
    }
 
    public void setN(final int x) {
        if (this.x1 != null && this.x2 != null) {
            if (x > this.maxX) {
                this.x1 = x;
                this.x2 = x;
            } else if (this.x1 <= this.x2) { this.x1 = x;
            } else { this.x2 = x;
            }
        } else if (this.x1 == null) { this.x1 = x;
        } else if (this.x2 == null) { this.x2 = x;
        }
        
        this.update();
    }
 
    public void setS(final int x) {
        if (this.x1 != null && this.x2 != null) {
            if (x < this.minX) {
                this.x1 = x;
                this.x2 = x;
            } else if (this.x1 >= this.x2) { this.x1 = x;
            } else { this.x2 = x;
            }
        } else if (this.x1 == null) { this.x1 = x;
        } else if (this.x2 == null) { this.x2 = x;
        }
        
        this.update();
    }
    
    public void setE(final int z) {
        if (this.z1 != null && this.z2 != null) {
            if (z > this.maxZ) {
                this.z1 = z;
                this.z2 = z;
            } else if (this.z1 <= this.z2) { this.z1 = z;
            } else { this.z2 = z;
            }
        } else if (this.z1 == null) { this.z1 = z;
        } else if (this.z2 == null) { this.z2 = z;
        }
        
        this.update();
    }
    
    public void setW(final int z) {
        if (this.z1 != null && this.z2 != null) {
            if (z < this.minZ) {
                this.z1 = z;
                this.z2 = z;
            } else if (this.z1 >= this.z2) { this.z1 = z;
            } else { this.z2 = z;
            }
        } else if (this.z1 == null) { this.z1 = z;
        } else if (this.z2 == null) { this.z2 = z;
        }
        
        this.update();
    }
    
    public void setU(final int y) {
        if (this.y1 != null && this.y2 != null) {
            if (y < this.minY) {
                this.y1 = y;
                this.y2 = y;
            } else if (this.y1 >= this.y2) { this.y1 = y;
            } else { this.y2 = y;
            }
        } else if (this.y1 == null) { this.y1 = y;
        } else if (this.y2 == null) { this.y2 = y;
        }
        
        this.update();
    }

    public void setD(final int y) {
        if (this.y1 != null && this.y2 != null) {
            if (y > this.maxY) {
                this.y1 = y;
                this.y2 = y;
            } else if (this.y1 <= this.y2) { this.y1 = y;
            } else { this.y2 = y;
            }
        } else if (this.y1 == null) { this.y1 = y;
        } else if (this.y2 == null) { this.y2 = y;
        }
        
        this.update();
    }
    
    /**
     * Generates a textual representation of the volumetric size.</br>
     * </br>
     * Example: 100x * 50y * 128z = 640,000 blocks
     * 
     * @return text-based representation of the volumetric size
     */
    public String describeVolume() {
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
            "%1$sx * %2$sy * %3$sz = %4$s blocks cubed"
            , (sizeX == null ? "?" : sizeX), (sizeY == null ? "?" : sizeY), (sizeZ == null ? "?" : sizeZ)
            , (sizeX != null && sizeY != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeY * sizeZ) : "?")
        );
        
        return size;
    }
    
    /**
     * Generates a textual representation of the two-dimensional area
     * across the x and z axes.</br>
     * </br>
     * Example: 100x * 128z = 12,800 square meters
     * 
     * @return text-based representation of the area
     */
    public String describeArea() {
        Integer sizeX = null;
        if (this.getN() != null && this.getS() != null)
            sizeX = Math.abs(this.getS() - this.getN()) + 1;
        
        Integer sizeZ = null;
        if (this.getE() != null && this.getW() != null)
            sizeZ = Math.abs(this.getW() - this.getE()) + 1;
        
        String area = String.format(
                "%1$sx * %2$sz = %3$s blocks squared"
                , (sizeX == null ? "?" : sizeX), (sizeZ == null ? "?" : sizeZ)
                , (sizeX != null && sizeZ != null ? new DecimalFormat().format(sizeX * sizeZ) : "?")
        );
        
        return area;
    }
    
    /**
     * Generates a textual representation of the coordinates.
     * 
     * @param format specifies the visual format to use
     * @return text-based visual representation
     */
    public String describeCoordinates(final Integer format) {
        int lengthMinX = 1;
        if (this.minX != null) lengthMinX = Integer.toString(this.minX).length();
        int lengthMinY = 1;
        if (this.minY != null) lengthMinY = Integer.toString(this.minY).length();
        int lengthMinZ = 1;
        if (this.minZ != null) lengthMinZ = Integer.toString(this.minZ).length();
        int longestMin = Math.max(lengthMinX, Math.max(lengthMinY, lengthMinZ));
        
        int lengthMaxX = 1;
        if (this.maxX != null) lengthMaxX = Integer.toString(this.maxX).length();
        int lengthMaxY = 1;
        if (this.maxY != null) lengthMaxY = Integer.toString(this.maxY).length();
        int lengthMaxZ = 1;
        if (this.maxZ != null) lengthMaxZ = Integer.toString(this.maxZ).length();
        int longestMax = Math.max(lengthMaxX, Math.max(lengthMaxY, lengthMaxZ));
        
        int longestX = Math.max(lengthMinX, lengthMaxX);
        int longestY = Math.max(lengthMinY, lengthMaxY);
     // int longestZ = Math.max(lengthMinZ, lengthMaxZ);
        
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
        
        switch ((format != null ? format : CachingRectangularCuboid.DEFAULT_COORDINATE_FORMAT)) {
            default:
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
    }
    
    @Override
    public String toString() {
        return "(x:" + this.x1 + ",y:" + this.y1 + ",z:" + this.z1 + ") - (x:" + this.x2 + ",y:" + this.y2 + ",z:" + this.z2 + ")";
    }
    
    /**
     * Update cached attributes.
     */
    private void update() {
        this.calculateDefined();
        this.calculateMinMax();
    }
    
    private void calculateDefined() {
        this.defined = (this.x1 != null && this.x2 != null && this.y1 != null && this.y2 != null && this.z1 != null && this.z2 != null);
    }
    
    /**
     * Determine minimums and maximums for axes to improve performance for
     * checking if this contains a given coordinate.
     */
    private void calculateMinMax() {
        // Cache X axis minimums/maximums
        this.minX = null;
        this.maxX = null;
        if (this.x1 != null && this.x2 != null) {
            this.minX = Math.min(this.x1, this.x2);
            this.maxX = Math.max(this.x1, this.x2);
        } else if (this.x1 != null) {
            this.minX = this.x1;
        } else if (this.x2 != null) {
            this.minX = this.x2;
        }
        
        // Cache Y axis minimums/maximums
        this.minY = null;
        this.maxY = null;
        if (this.y1 != null && this.y2 != null) {
            this.minY = Math.min(this.y1, this.y2);
            this.maxY = Math.max(this.y1, this.y2);
        } else if (this.y1 != null) {
            this.minY = this.y1;
        } else if (this.y2 != null) {
            this.minY = this.y2;
        }
        
        // Cache Z axis minimums/maximums
        this.minZ = null;
        this.maxZ = null;
        if (this.z1 != null && this.z2 != null) {
            this.minZ = Math.min(this.z1, this.z2);
            this.maxZ = Math.max(this.z1, this.z2);
        } else if (this.z1 != null) {
            this.minZ = this.z1;
        } else if (this.z2 != null) {
            this.minZ = this.z2;
        }
        
        // Cache chunk minimums/maximums
        this.minChunkX = null;
        this.maxChunkX = null;
        this.minChunkZ = null;
        this.maxChunkZ = null;
        if (!this.defined) return;
        
        this.minChunkX = this.minX >> 4;
        this.maxChunkX = this.maxX >> 4;
        this.minChunkZ = this.minZ >> 4;
        this.maxChunkZ = this.maxZ >> 4;
    }
    
    /**
     * Generate a repeating string of s, n times.
     * 
     * @param s string to repeat
     * @param n number of times to repeat s
     * @return string of s repeated n times
     */
    private static String repeat(final String s, int n) {
        return new String(new char[n]).replace("\0", s);
    }
}