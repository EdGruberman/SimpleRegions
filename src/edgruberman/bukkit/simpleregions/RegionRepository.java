package edgruberman.bukkit.simpleregions;

import java.util.Collection;

import org.bukkit.World;

public interface RegionRepository {

    public abstract Collection<Region> loadRegions(final World world);

    public abstract void saveRegion(final Region region, final boolean immediate);

    public abstract void deleteRegion(final Region region, final boolean immediate);

}
