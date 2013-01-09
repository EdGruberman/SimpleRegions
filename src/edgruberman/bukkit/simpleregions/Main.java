package edgruberman.bukkit.simpleregions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;

import edgruberman.bukkit.simpleregions.commands.Current;
import edgruberman.bukkit.simpleregions.commands.Describe;
import edgruberman.bukkit.simpleregions.commands.Reload;
import edgruberman.bukkit.simpleregions.commands.Target;
import edgruberman.bukkit.simpleregions.commands.design.Attach;
import edgruberman.bukkit.simpleregions.commands.design.Clear;
import edgruberman.bukkit.simpleregions.commands.design.Context;
import edgruberman.bukkit.simpleregions.commands.design.Create;
import edgruberman.bukkit.simpleregions.commands.design.Define;
import edgruberman.bukkit.simpleregions.commands.design.Delete;
import edgruberman.bukkit.simpleregions.commands.design.Detach;
import edgruberman.bukkit.simpleregions.commands.design.Rename;
import edgruberman.bukkit.simpleregions.commands.manage.Activate;
import edgruberman.bukkit.simpleregions.commands.manage.Deactivate;
import edgruberman.bukkit.simpleregions.commands.manage.Demote;
import edgruberman.bukkit.simpleregions.commands.manage.Farewell;
import edgruberman.bukkit.simpleregions.commands.manage.Grant;
import edgruberman.bukkit.simpleregions.commands.manage.Greeting;
import edgruberman.bukkit.simpleregions.commands.manage.Promote;
import edgruberman.bukkit.simpleregions.commands.manage.Reform;
import edgruberman.bukkit.simpleregions.commands.manage.Replace;
import edgruberman.bukkit.simpleregions.commands.manage.Revoke;
import edgruberman.bukkit.simpleregions.messaging.ConfigurationCourier;
import edgruberman.bukkit.simpleregions.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    private Catalog catalog = null;

    @Override
    public void onLoad() {
        this.putConfigMinimum("4.4.0a11");
        this.putConfigMinimum("messages.yml", "4.4.0a11");
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.Factory.create(this).setBase(this.loadConfig("messages.yml")).setColorCode("color-code").build();

        this.extractConfig("defaults.yml", false);
        final Repository repository = new Repository(this, new File(this.getDataFolder(), "defaults.yml"), new File(this.getDataFolder(), "Worlds"));
        this.catalog = new Catalog(this, repository, this.getConfig().getConfigurationSection("options"));
        Bukkit.getPluginManager().registerEvents(new Guard(this.catalog, this.parseMaterialList("target-face"), this.getConfig().getBoolean("protect-fire")), this);
        Bukkit.getPluginManager().registerEvents(new BoundaryAlerter(this.catalog), this);

        this.getCommand("simpleregions:reload").setExecutor(new Reload(this));
        this.getCommand("simpleregions:current").setExecutor(new Current(this.catalog));
        this.getCommand("simpleregions:target").setExecutor(new Target(this.catalog));
        this.getCommand("simpleregions:context").setExecutor(new Context(this.catalog));
        this.getCommand("simpleregions:clear").setExecutor(new Clear(this.catalog));
        this.getCommand("simpleregions:describe").setExecutor(new Describe(this.catalog));
        this.getCommand("simpleregions:activate").setExecutor(new Activate(this.catalog));
        this.getCommand("simpleregions:deactivate").setExecutor(new Deactivate(this.catalog));
        this.getCommand("simpleregions:promote").setExecutor(new Promote(this.catalog));
        this.getCommand("simpleregions:demote").setExecutor(new Demote(this.catalog));
        this.getCommand("simpleregions:reform").setExecutor(new Reform(this.catalog));
        this.getCommand("simpleregions:grant").setExecutor(new Grant(this.catalog));
        this.getCommand("simpleregions:revoke").setExecutor(new Revoke(this.catalog));
        this.getCommand("simpleregions:replace").setExecutor(new Replace(this.catalog));
        this.getCommand("simpleregions:greeting").setExecutor(new Greeting(this.catalog));
        this.getCommand("simpleregions:farewell").setExecutor(new Farewell(this.catalog));
        this.getCommand("simpleregions:create").setExecutor(new Create(this.catalog));
        this.getCommand("simpleregions:define").setExecutor(new Define(this.catalog));
        this.getCommand("simpleregions:delete").setExecutor(new Delete(this.catalog));
        this.getCommand("simpleregions:rename").setExecutor(new Rename(this.catalog));
        this.getCommand("simpleregions:attach").setExecutor(new Attach(this.catalog));
        this.getCommand("simpleregions:detach").setExecutor(new Detach(this.catalog));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        this.catalog.clear();
        Main.courier = null;
    }

    private List<Material> parseMaterialList(final String path) {
        final List<Material> materials = new ArrayList<Material>();
        for (final String name : this.getConfig().getStringList(path)) {
            final Material material = Material.getMaterial(name);
            if (material == null) {
                this.getLogger().warning("Unrecognized Material in " + path + ": " + name);
                continue;
            }

            materials.add(material);
        }
        return materials;
    }

}
