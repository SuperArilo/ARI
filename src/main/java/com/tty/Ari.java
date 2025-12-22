package com.tty;

import com.google.gson.reflect.TypeToken;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.function.PlayerTabManager;
import com.tty.lib.Log;
import com.tty.lib.ServerPlatform;
import com.tty.lib.command.CommandRegister;
import com.tty.lib.dto.AliasItem;
import com.tty.lib.services.ConfigDataService;
import com.tty.lib.services.StateService;
import com.tty.lib.tool.*;
import com.tty.listener.GuiCleanupListener;
import com.tty.listener.OnPluginReloadListener;
import com.tty.listener.PlayerListener;
import com.tty.listener.home.EditHomeListener;
import com.tty.listener.home.HomeListListener;
import com.tty.listener.player.*;
import com.tty.listener.skip_sleep.PlayerSkipNight;
import com.tty.listener.teleport.RecordLastLocationListener;
import com.tty.listener.warp.EditWarpListener;
import com.tty.listener.warp.WarpListListener;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.tool.*;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

import static com.tty.lib.tool.PublicFunctionUtils.checkServerVersion;


public class Ari extends JavaPlugin {

    public static Ari instance;
    public static boolean PLUGIN_IS_DISABLED = false;
    public static Boolean DEBUG = false;
    public static final ConfigInstance C_INSTANCE = new ConfigInstance();
    public SQLInstance sqlInstance;
    public WebServer webServer;

    public ConfigDataService dataService;

    public StateMachineManager stateMachineManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if(!checkServerVersion()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        reloadAllConfig();
        Log.init(this.getComponentLogger(), DEBUG);

        this.printLogo();

        PublicFunctionUtils.loadPlugin("Vault", Economy.class, EconomyUtils::setInstance);
        PublicFunctionUtils.loadPlugin("Vault", Permission.class, PermissionUtils::setInstance);
        PublicFunctionUtils.loadPlugin("arilib", ConfigDataService.class, i -> this.dataService = i);

        this.stateMachineManager = new StateMachineManager(this);
        this.stateMachineManager.initDefaultStateMachines();

        this.registerListener();
        CommandRegister.register(this, "com.tty.commands", FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.COMMAND_ALIAS.name()).saveToString(), new TypeToken<Map<String, AliasItem>>() {}.getType()));

        this.sqlInstance = new SQLInstance();
        this.sqlInstance.start();

        //初始化rtp
        RandomTpStateService.setRtpWorldConfig();

        this.webServer = new WebServer();
        this.webServer.start(this);

    }
    @Override
    public void onDisable() {
        PLUGIN_IS_DISABLED = true;
        if (this.stateMachineManager != null) {
            this.stateMachineManager.forEach(StateService::abort);
        }
        SQLInstance.close();
        if (this.webServer != null) this.webServer.stop();

        C_INSTANCE.clearConfigs();
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new GuiCleanupListener(), this);
        pluginManager.registerEvents(new HomeListListener(GuiType.HOME_LIST), this);
        pluginManager.registerEvents(new EditHomeListener(GuiType.HOME_EDIT), this);
        pluginManager.registerEvents(new RecordLastLocationListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new WarpListListener(GuiType.WARP_LIST), this);
        pluginManager.registerEvents(new EditWarpListener(GuiType.WARP_EDIT), this);
        pluginManager.registerEvents(new OnPlayerJoinAndLeaveListener(), this);
        pluginManager.registerEvents(new PlayerSkipNight(), this);
        pluginManager.registerEvents(new OnPluginReloadListener(), this);
        pluginManager.registerEvents(new PlayerTabManager(), this);
        pluginManager.registerEvents(new CustomChatFormantListener(), this);
        pluginManager.registerEvents(new PlayerActionListener(), this);
        pluginManager.registerEvents(new KeepInventoryAndExperience(), this);
        pluginManager.registerEvents(new CustomPlayerDeathListener(), this);
    }

    public static void reloadAllConfig() {
        Ari.instance.saveDefaultConfig();
        Ari.instance.reloadConfig();
        DEBUG = Ari.instance.getConfig().getBoolean("debug.enable", false);
        loadConfigInMemory();
    }
    private static void loadConfigInMemory() {
        C_INSTANCE.clearConfigs();
        FileConfiguration pluginConfig = Ari.instance.getConfig();
        for (FilePath filePath : FilePath.values()) {
            String path = filePath.getPath().replace("[lang]", Ari.instance.getConfig().getString("lang", "cn"));
            File file = new File(Ari.instance.getDataFolder(), path);
            if (!file.exists()) {
                Ari.instance.saveResource(path, true);
            } else if (pluginConfig.getBoolean("debug.overwrite-file", false)) {
                try {
                    Ari.instance.saveResource(path, true);
                } catch (Exception e) {
                    Log.error("can not find file %s, path %s .", filePath.getNickName(), path);
                }
            }
            C_INSTANCE.setConfig(filePath.name(), YamlConfiguration.loadConfiguration(file));
        }
    }


    @SuppressWarnings("deprecation")
    private void printLogo() {
        String pluginInfo;
        if (ServerPlatform.isFolia()) {
            PluginMeta pluginMeta = Ari.instance.getPluginMeta();
            pluginInfo = pluginMeta.getName() + " " + pluginMeta.getVersion();
        } else {
            PluginDescriptionFile description = Ari.instance.getDescription();
            pluginInfo = description.getName() + " " + description.getVersion();
        }
        String bukkitName = Bukkit.getName();
        String bukkitVersion = Bukkit.getServer().getVersion();
        Log.info("");
        Log.info("        _   ");
        Log.info("  |    /_\\  %s", pluginInfo);
        Log.info("  |___/   \\ Running on %s %s", bukkitName, bukkitVersion);
        Log.info("");
    }
}
