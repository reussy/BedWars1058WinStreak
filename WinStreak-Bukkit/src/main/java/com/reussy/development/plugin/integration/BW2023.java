package com.reussy.development.plugin.integration;

import com.reussy.development.plugin.util.DebugUtil;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;

public class BW2023 implements IPluginIntegration {
    private com.tomkeuper.bedwars.api.BedWars bedWars;

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
        return Bukkit.getPluginManager().getPlugin("BedWars2023") != null;
    }

    /**
     * @return true If the plugin is enabled by Private Miner, otherwise false.
     */
    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("BedWars2023");
    }


    /**
     * Enable and instance the plugin
     */
    @Override
    public boolean enable() {

        if (isPresent()) {
            this.bedWars = Bukkit.getServicesManager().getRegistration(com.tomkeuper.bedwars.api.BedWars.class).getProvider();
            DebugUtil.printBukkit("&7Using &3BedWars2023 &7as Bed Wars core.");
            return true;
        }
        return false;
    }

    /**
     * Disable the instance of the plugin.
     */
    @Override
    public void disable() {

    }

    public BedWars get() {
        return bedWars;
    }
}
