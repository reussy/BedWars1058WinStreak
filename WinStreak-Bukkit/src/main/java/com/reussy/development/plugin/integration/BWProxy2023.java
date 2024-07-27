package com.reussy.development.plugin.integration;

import com.reussy.development.plugin.util.DebugUtil;
import org.bukkit.Bukkit;

public class BWProxy2023 implements IPluginIntegration {

    /**
     * @return true If this plugin was hooked successfully, otherwise false.
     */
    @Override
    public boolean isRunning() {
        return isPresent() && isEnabled();
    }

    /**
     * @return true If the plugin is present, otherwise false.
     */
    @Override
    public boolean isPresent() {
        return Bukkit.getPluginManager().getPlugin("BWProxy2023") != null;
    }

    /**
     * @return true If the plugin is enabled by Private Miner, otherwise false.
     */
    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("BWProxy2023");
    }

    /**
     * Enable and instance the plugin for Private Miner.
     */
    @Override
    public boolean enable() {
        if (isPresent()) {
            DebugUtil.printBukkit("&7Using &3BedWarsProxy2023 &7as Bed Wars core.");
            return true;
        }

        return false;
    }

    /**
     * Disable the plugin for Private Miner.
     */
    @Override
    public void disable() {

    }
}
