package com.tty;

import com.google.gson.reflect.TypeToken;
import com.tty.enumType.FilePath;
import com.tty.function.*;
import com.tty.lib.enum_type.GuiType;
import com.tty.lib.Log;
import com.tty.lib.ServerPlatform;
import com.tty.lib.command.CommandRegister;
import com.tty.lib.dto.AliasItem;
import com.tty.lib.services.ConfigDataService;
import com.tty.lib.services.NBTDataService;
import com.tty.lib.services.StateService;
import com.tty.lib.tool.*;
import com.tty.listener.*;
import com.tty.listener.home.EditHomeListener;
import com.tty.listener.home.HomeListListener;
import com.tty.listener.player.*;
import com.tty.listener.teleport.RecordLastLocationListener;
import com.tty.listener.warp.EditWarpListener;
import com.tty.listener.warp.WarpListListener;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.tool.*;
import com.tty.tool.Placeholder;
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


@SuppressWarnings("UnstableApiUsage")
public class Ari extends JavaPlugin {

    public static Ari instance;
    public static Boolean DEBUG = false;
    public static final ConfigInstance C_INSTANCE = new ConfigInstance();
    public static SQLInstance SQL_INSTANCE;
    public static final RepositoryManager REPOSITORY_MANAGER = new RepositoryManager();
    public static ConfigDataService DATA_SERVICE;
    public static NBTDataService NBT_DATA_SERVICE;
    public static StateMachineManager STATE_MACHINE_MANAGER;
    public static Placeholder PLACEHOLDER;

    @Override
    public void onLoad() {
        instance = this;
        reloadAllConfig();
        Log.init(this.getLogger(), DEBUG);
        this.printLogo();
    }

    @Override
    public void onEnable() {
        if(!checkServerVersion()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        SQL_INSTANCE = new SQLInstance();
        STATE_MACHINE_MANAGER = new StateMachineManager(this);
        PublicFunctionUtils.loadPlugin("Vault", Economy.class, EconomyUtils::setInstance);
        PublicFunctionUtils.loadPlugin("Vault", Permission.class, PermissionUtils::setInstance);
        PublicFunctionUtils.loadPlugin("arilib", ConfigDataService.class, i -> DATA_SERVICE = i);
        PublicFunctionUtils.loadPlugin("arilib", NBTDataService.class, i -> NBT_DATA_SERVICE = i);

        //初始化rtp
        RandomTpStateService.setRtpWorldConfig();

        this.registerListener();
        CommandRegister.register(this, "com.tty.commands", FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.COMMAND_ALIAS.name()).saveToString(), new TypeToken<Map<String, AliasItem>>() {}.getType()));

        PLACEHOLDER = new Placeholder();

    }

    @Override
    public void onDisable() {
        if (STATE_MACHINE_MANAGER != null) {
            STATE_MACHINE_MANAGER.forEach(StateService::abort);
        }
        REPOSITORY_MANAGER.clearAllCache();
        SQL_INSTANCE.close();
        C_INSTANCE.clearConfigs();
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new DamageTrackerListener(), this);
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
        pluginManager.registerEvents(new BreakAndExplodeListener(), this);
        pluginManager.registerEvents(new AutoSeedListener(), this);
        pluginManager.registerEvents(new MobBossBarListener(), this);
        pluginManager.registerEvents(new DisableMobSpawnListener(), this);
        pluginManager.registerEvents(new CustomTotemCostListener(), this);
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
                    Log.error("can not find file {}, path {} .", filePath.getNickName(), path);
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
        Log.info("  |    /_\\  {}", pluginInfo);
        Log.info("  |___/   \\ Running on {} {}", bukkitName, bukkitVersion);
        Log.info("");
    }
}
