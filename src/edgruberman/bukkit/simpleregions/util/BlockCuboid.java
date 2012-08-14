package edgruberman.bukkit.simpleregions.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;

/** block based cell with rectangular faces */
public class BlockCuboid {

    protected Integer x1 = null, x2 = null, y1 = null, y2 = null, z1 = null, z2 = null;
    protected Integer minX = null, maxX = null, minY = null, maxY = null, minZ = null, maxZ = null;
    protected Integer minChunkX = null, maxChunkX = null, minChunkZ = null, maxChunkZ = null;

    /**
     * @return true if block at coordinates is inside; false otherwise
     * @throws NullPointerException when not {@link #isDefined}
     */
    public boolean contains(final int x, final int y, final int z) {
        return !(x < this.minX || x > this.maxX || z < this.minZ || z > this.maxZ || y < this.minY || y > this.maxY);
    }

    /**
     * @return true if block at location is inside; false otherwise
     * @throws NullPointerException when not {@link #isDefined}
     */
    public boolean contains(final Location loc) {
        return this.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * @return true if chunk contains any block that is inside cuboid
     * @throws NullPointerException when not {@link #isDefined}
     */
    public boolean within(final int chunkX, final int chunkZ) {
        return chunkX >= this.minChunkX && chunkX <= this.maxChunkX && chunkZ >= this.minChunkZ && chunkZ <= this.maxChunkZ;
    }

    /** @return list of chunks that contain at least one block of this cuboid */
    public List<ChunkCoordinates> chunks() {
        if (!this.isDefined()) return Collections.<ChunkCoordinates>emptyList();

        final List<ChunkCoordinates> coords = new ArrayList<ChunkCoordinates>();
        for (int x = this.minChunkX; x <= this.maxChunkX; x++)
            for (int z = this.minChunkZ; z <= this.maxChunkZ; z++)
                coords.add(new ChunkCoordinates(x, z));

        return coords;
    }

    /** @return true if all coordinates configured */
    public boolean isDefined() {
        return (this.x1 != null && this.x2 != null && this.y1 != null && this.y2 != null && this.z1 != null && this.z2 != null);
    }

    public Integer getX1() { return this.x1; }
    public Integer getX2() { return this.x2; }
    public Integer getY1() { return this.y1; }
    public Integer getY2() { return this.y2; }
    public Integer getZ1() { return this.z1; }
    public Integer getZ2() { return this.z2; }

    public Integer getMinX() { return this.minX; }
    public Integer getMaxX() { return this.maxX; }
    public Integer getMinY() { return this.minY; }
    public Integer getMaxY() { return this.maxY; }
    public Integer getMinZ() { return this.minZ; }
    public Integer getMaxZ() { return this.maxZ; }

    public Integer getMinChunkX() { return this.minChunkX; }
    public Integer getMaxChunkX() { return this.maxChunkX; }
    public Integer getMinChunkZ() { return this.minChunkZ; }
    public Integer getMaxChunkZ() { return this.maxChunkZ; }

    public void setX1(final int x) {
        if (this.x1 != null && this.x1 == x) return;

        this.x1 = x;
        this.refresh();
    }

    public void setX2(final int x) {
        if (this.x2 != null && this.x2 == x) return;

        this.x2 = x;
        this.refresh();
    }

    public void setY1(final int y) {
        if (this.y1 != null && this.y1 == y) return;

        this.y1 = y;
        this.refresh();
    }

    public void setY2(final int y) {
        if (this.y2 != null && this.y2 == y) return;

        this.y2 = y;
        this.refresh();
    }

    public void setZ1(final int z) {
        if (this.z1 != null && this.z1 == z) return;

        this.z1 = z;
        this.refresh();
    }

    public void setZ2(final int z) {
        if (this.z2 != null && this.z2 == z) return;

        this.z2 = z;
        this.refresh();
    }

    public void setVertex1(final int x, final int y, final int z) {
        if (this.x1 != null && this.y1 != null && this.z1 != null && this.x1 == x && this.y1 == y && this.z1 == z) return;

        this.x1 = x;
        this.y1 = y;
        this.z1 = z;
        this.refresh();
    }

    public void setVertex2(final int x, final int y, final int z) {
        if (this.x2 != null && this.y2 != null && this.z2 != null && this.x2 == x && this.y2 == y && this.z2 == z) return;

        this.x2 = x;
        this.y2 = y;
        this.z2 = z;
        this.refresh();
    }

    public void setMinX(final int x) {
        if (this.minX != null && this.minX == x) return;

        this.minX = x;
        this.maxX = Math.max(this.minX, this.coalesce(this.maxX, this.minX));
        this.x1 = this.minX;
        this.x2 = this.maxX;
        this.refresh();
    }

    public void setMaxX(final int x) {
        if (this.maxX != null && this.maxX == x) return;

        this.maxX = x;
        this.minX = Math.min(this.coalesce(this.minX, this.maxX), this.maxX);
        this.x1 = this.minX;
        this.x2 = this.maxX;
        this.refresh();
    }

    public void setMinY(final int y) {
        if (this.minY != null && this.minY == y) return;

        this.minY = y;
        this.maxY = Math.max(this.minY, this.coalesce(this.maxY, this.minY));
        this.y1 = this.minY;
        this.y2 = this.maxY;
        this.refresh();
    }

    public void setMaxY(final int y) {
        if (this.maxY != null && this.maxY == y) return;

        this.maxY = y;
        this.minY = Math.min(this.coalesce(this.minY, this.maxY), this.maxY);
        this.y1 = this.minY;
        this.y2 = this.maxY;
        this.refresh();
    }

    public void setMinZ(final int z) {
        if (this.minZ != null && this.minZ == z) return;

        this.minZ = z;
        this.maxZ = Math.max(this.minZ, this.coalesce(this.maxZ, this.minZ));
        this.z1 = this.minZ;
        this.z2 = this.maxZ;
        this.refresh();
    }

    public void setMaxZ(final int z) {
        if (this.maxZ != null && this.maxZ == z) return;

        this.maxZ = z;
        this.minZ = Math.min(this.coalesce(this.minZ, this.maxZ), this.maxZ);
        this.z1 = this.minZ;
        this.z2 = this.maxZ;
        this.refresh();
    }

    protected void refresh() {
        if (!this.isDefined()) return;

        this.minX = Math.min(this.x1, this.x2);
        this.maxX = Math.max(this.x1, this.x2);
        this.minY = Math.min(this.y1, this.y2);
        this.maxY = Math.max(this.y1, this.y2);
        this.minZ = Math.min(this.z1, this.z2);
        this.maxZ = Math.max(this.z1, this.z2);

        this.minChunkX = this.minX >> 4;
        this.maxChunkX = this.maxX >> 4;
        this.minChunkZ = this.minZ >> 4;
        this.maxChunkZ = this.maxZ >> 4;
    }

    @Override
    public String toString() {
        return "(x1:" + this.x1 + ",y1:" + this.y1 + ",z1:" + this.z1 + ") - (x2:" + this.x2 + ",y2:" + this.y2 + ",z2:" + this.z2 + ")";
    }

    /** null coalescing operator; because it reads /slightly/ better */
    private Integer coalesce(final Integer i1, final Integer i2) {
        return (i1 != null ? i1 : i2);
    }

}
