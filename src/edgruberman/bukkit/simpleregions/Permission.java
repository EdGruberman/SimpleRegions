package edgruberman.bukkit.simpleregions;

import edgruberman.bukkit.simpleregions.commands.RegionCurrent;
import edgruberman.bukkit.simpleregions.commands.RegionReload;

public enum Permission {
      REGION(edgruberman.bukkit.simpleregions.commands.Region.NAME)
    
    , REGION_RELOAD(Permission.REGION.append(RegionReload.NAME))
    
    , REGION_CURRENT(Permission.REGION.append(RegionCurrent.NAME)) 
    
    ;
    
    static final String PLUGIN_NAME = "simpleregions";
    
    private final String format;
    
    private Permission(String format) {
        this.format = format;
    }
    
    @Override
    public String toString() {
        return this.toString((Object) null);
    }
    
    public String toString(Object... args) {
        return String.format(Permission.PLUGIN_NAME + "." + this.format, args);
    }
    
    private String append(final String path) {
        return this.format + "." + path;
    }
}