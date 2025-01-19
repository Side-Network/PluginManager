package net.lenni0451.spm;

import com.tchristofferson.configupdater.ConfigUpdater;
import net.lenni0451.spm.commands.PluginManager_Command;
import net.lenni0451.spm.commands.Reload_Command;
import net.lenni0451.spm.messages.I18n;
import net.lenni0451.spm.tabcomplete.PluginManager_TabComplete;
import net.lenni0451.spm.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

public class PluginManager extends JavaPlugin {

    private static PluginManager instance;

    /**
     * Get the instance of {@link PluginManager}
     *
     * @return The instance
     */
    public static PluginManager getInstance() {
        return instance;
    }


    private final PluginUtils pluginUtils;
    private final InstalledPluginsConfig installedPluginsInfo;

    public PluginManager() {
        instance = this;

        this.saveDefaultConfig();
        try {
            ConfigUpdater.update(this, "config.yml", new File(this.getDataFolder(), "config.yml"));
        } catch (Throwable t) {
            throw new RuntimeException("Unable to update config to the latest version", t);
        }
        I18n.init();

        this.pluginUtils = new PluginUtils();
        this.installedPluginsInfo = new InstalledPluginsConfig();
    }

    /**
     * Get the instance of {@link PluginUtils}
     *
     * @return The instance
     */
    public PluginUtils getPluginUtils() {
        return this.pluginUtils;
    }

    /**
     * Get the instance of {@link InstalledPluginsConfig}
     *
     * @return The instance
     */
    public InstalledPluginsConfig getInstalledPlugins() {
        return this.installedPluginsInfo;
    }


    @Override
    public void onEnable() {
        this.getCommand("reload").setExecutor(new Reload_Command());
        this.getCommand("reload").setAliases(Collections.singletonList("rl"));

        this.getCommand("pluginmanager").setExecutor(new PluginManager_Command());
        this.getCommand("pluginmanager").setAliases(Collections.singletonList("pm"));
        this.getCommand("pluginmanager").setTabCompleter(new PluginManager_TabComplete());

        if (I18n.wasUpdated()) {
            Bukkit.getScheduler().runTask(PluginManager.getInstance(), () -> {
                for (String translationLine : I18n.mt("pm.updater.missingTranslations")) {
                    Logger.sendConsole("§a" + translationLine);
                }
            });
        }
    }

}
