package com.reussy.development.plugin;

import com.andrei1058.bedwars.api.server.ServerType;
import com.reussy.development.api.WinStreakAPI;
import com.reussy.development.api.user.IUser;
import com.reussy.development.plugin.command.StreakAdminCommand;
import com.reussy.development.plugin.command.StreakCommand;
import com.reussy.development.plugin.command.StreakCommandProxy;
import com.reussy.development.plugin.config.PluginConfiguration;
import com.reussy.development.plugin.event.UserCache;
import com.reussy.development.plugin.event.UserInGame;
import com.reussy.development.plugin.integration.*;
import com.reussy.development.plugin.repository.UserRepository;
import com.reussy.development.plugin.storage.IStorage;
import com.reussy.development.plugin.storage.service.MySQL;
import com.reussy.development.plugin.storage.service.SQLite;
import com.reussy.development.plugin.util.DebugUtil;
import com.reussy.development.plugin.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;

public class WinStreakPlugin extends JavaPlugin {

    private static WinStreakAPI api;
    private BW1058 BW1058;
    private com.reussy.development.plugin.integration.BW2023 BW2023;
    private BWProxy BWProxyIntegration;
    private BWProxy2023 BWProxy2023;
    private PAPI PAPI;
    private IStorage IStorage;
    private PluginConfiguration pluginConfiguration;
    private ServerInstance serverInstance;

    @Override
    public void onLoad() {
        api = new API();
        Bukkit.getServicesManager().register(WinStreakAPI.class, api, this, ServicePriority.High);
    }

    @Override
    public void onEnable() {

        this.pluginConfiguration = new PluginConfiguration(this);

        final long start = System.currentTimeMillis();
        DebugUtil.separator();
        DebugUtil.printBukkit("&7Loading &cWinStreak add-on &7" + getDescription().getVersion() + " ...");
        DebugUtil.printBukkit("&7The add-on was developed by &9HUH! Development. &7Much love ❤");
        DebugUtil.printBukkit("&7Running on &c" + Bukkit.getVersion() + " &7fork &c" + Bukkit.getBukkitVersion() + "&7.");
        DebugUtil.empty();

        // Initialize all API utilities / wrappers / classes.
        DebugUtil.printBukkit("&7Initializing API classes, utilities and wrappers...");
        new UserRepository();
        DebugUtil.printBukkit("&cWinStreak add-on API &7initialized successfully, the API is ready to use.");
        DebugUtil.empty();

        if (!PluginUtil.isBedWarsCorePlugin()) return;

        // Once plugin utilities are initialized, initialize plugin integrations.
        DebugUtil.printBukkit("&7Initializing plugin integrations...");
        populateIntegrations(this.BW1058 = new BW1058(),
                this.BWProxyIntegration = new BWProxy(),
                this.BW2023 = new BW2023(),
                this.BWProxy2023 = new BWProxy2023(),
                this.PAPI = new PAPI(this));

        setServerInstance();

        // Once all plugin integrations are initialized, initialize the add-on configurations.
        this.pluginConfiguration = new PluginConfiguration(this);

        // Once all the integrations are initialized, we can register the storage and data property.
        DebugUtil.printBukkit("&7Registering storage service...");
        setupStorage();

        setupEvents();
        setupCommands();

        DebugUtil.empty();
        DebugUtil.printBukkit("&cWinStreak add-on &7" + getDescription().getVersion() + " &7loaded in &3" + (System.currentTimeMillis() - start) + "ms&7.");
        DebugUtil.separator();
    }

    @Override
    public void onDisable() {

        Bukkit.getOnlinePlayers().forEach(player ->{
            IUser user = getAPI().getUserUtil().getUser(player.getUniqueId());
            getDatabaseManager().saveUser(user);
        });

        this.getDatabaseManager().close();
    }

    public void debug(String message) {

        if (getFilesManager().getPluginConfig().getBoolean("general.debug")) {
            this.getLogger().info("[BW1058-WinStreak DEBUG]: " + message);
        }
    }

    private void populateIntegrations(IPluginIntegration @NotNull ... integrations) {
        for (IPluginIntegration integration : integrations) {
            integration.enable();
        }
    }

    private void setServerInstance(){
        if (getBW1058().isRunning()){
            if (getBW1058().get().getServerType() == ServerType.MULTIARENA){
                this.serverInstance = ServerInstance.GAME_SERVER;
            } else if (getBW1058().get().getServerType() == ServerType.BUNGEE){
                this.serverInstance = ServerInstance.HUB_SERVER;
            } else {
                this.serverInstance = ServerInstance.SHARED_SERVER;
            }
        } else if (getBW2023().isRunning()){
            if (getBW2023().get().getServerType() == com.tomkeuper.bedwars.api.server.ServerType.MULTIARENA){
                this.serverInstance = ServerInstance.GAME_SERVER;
            } else if (getBW2023().get().getServerType() == com.tomkeuper.bedwars.api.server.ServerType.BUNGEE){
                this.serverInstance = ServerInstance.HUB_SERVER;
            } else {
                this.serverInstance = ServerInstance.SHARED_SERVER;
            }
        }
        DebugUtil.printBukkit("&7The add-on has detected automatically the server type as &6" + this.serverInstance.name());
        DebugUtil.empty();
    }

    public ServerInstance getServerInstance(){
        return serverInstance;
    }

    private void setupStorage() {

        if (getBW1058().isRunning() || getBW2023().isRunning()) {

            if (!getBW1058().get().getConfigs().getMainConfig().getBoolean("database.enable")) {
                DebugUtil.printBukkit("&7Storage service &cSQLite &7registered.");
                this.IStorage = new SQLite(this);
            } else {
                DebugUtil.printBukkit("&7Storage service &cMySQL &7registered.");
                this.IStorage = new MySQL(this);
            }
        } else if (getBWProxy().isRunning() || getBWProxy2023().isRunning()) {
            File proxyConfig = new File("plugins/BedWarsProxy/config.yml");
            YamlConfiguration configYaml = YamlConfiguration.loadConfiguration(proxyConfig);

            if (!configYaml.getBoolean("database.enable")) {
                DebugUtil.printBukkit("&7Storage service &cSQLite &7registered.");
                this.IStorage = new SQLite(this);
            } else {
                DebugUtil.printBukkit("&7Storage service &cMySQL &7registered.");
                this.IStorage = new MySQL(this);
            }
        }
    }

    private void setupEvents() {

        Bukkit.getPluginManager().registerEvents(new UserCache(this), this);
        if (getBW1058().isRunning()) {
            Bukkit.getPluginManager().registerEvents(new UserInGame.InGame1058(this), this);
        } else if (getBW2023().isRunning()) {
            Bukkit.getPluginManager().registerEvents(new UserInGame.InGame2023(this), this);
        }
    }

    private void setupCommands() {

        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register("winstreak", new StreakAdminCommand(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (getBW1058().isRunning() && getBW1058().get().getServerType() != ServerType.BUNGEE) {
            new StreakCommand.StreakCommand1058(this, getBW1058().get().getBedWarsCommand(), "streak");
        } else if (getBW2023().isRunning() && getBW2023().get().getServerType() != com.tomkeuper.bedwars.api.server.ServerType.BUNGEE) {
            new StreakCommand.StreakCommand2023(this, getBW2023().get().getBedWarsCommand(), "streak");
        } else {
            if (getBWProxy().isRunning() || getBWProxy2023().isRunning()) {
                try {
                    Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                    bukkitCommandMap.setAccessible(true);
                    CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
                    commandMap.register("winstreak", new StreakCommandProxy(this));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public WinStreakAPI getAPI() {
        return api;
    }

    public BW1058 getBW1058() {
        return BW1058;
    }

    public com.reussy.development.plugin.integration.BW2023 getBW2023() {
        return BW2023;
    }

    public BWProxy getBWProxy() {
        return BWProxyIntegration;
    }

    public BWProxy2023 getBWProxy2023() {
        return BWProxy2023;
    }

    public PAPI getPlaceholderAPI() {
        return PAPI;
    }

    public IStorage getDatabaseManager() {
        return IStorage;
    }

    public PluginConfiguration getFilesManager() {
        return pluginConfiguration;
    }
}
