package edgruberman.bukkit.simpleregions.options;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.simpleregions.Region;

public class Option {

    public static Option create(final Plugin plugin, final String className) throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        final Class<? extends Option> subClass = Option.find(className);
        final Constructor<? extends Option> ctr = subClass.getConstructor(Plugin.class);
        return ctr.newInstance(plugin);
    }

    public static Class<? extends Option> find(final String className) throws ClassNotFoundException, ClassCastException {
        // Look in local package first
        try {
            return Class.forName(Option.class.getPackage().getName() + "." + className).asSubclass(Option.class);
        } catch (final Exception e) {
            // Ignore to try searching for custom class next
        }

        // Look for a custom class
        return Class.forName(className).asSubclass(Option.class);
    }



    protected final Plugin plugin;
    protected final Set<Region> regions = new HashSet<Region>();

    protected Option(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().config("Initializing region option: " + this);
    }

    protected void start() {};

    protected void stop() {};

    public boolean register(final Region region) {
        final boolean added = this.regions.add(region);
        if (this.regions.size() == 1) {
            this.plugin.getLogger().log(Level.FINEST, "Starting region option: {0}", this);
            this.start();
        }
        return added;
    }

    public boolean deregister(final Region region) {
        final boolean removed = this.regions.remove(region);
        if (this.regions.size() == 0) {
            this.plugin.getLogger().log(Level.FINEST, "Stopping region option: {0}", this);
            this.stop();
        }
        return removed;
    }

    public void clear() {
        this.stop();
        this.regions.clear();
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this.getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}
