package com.tty;

import com.google.gson.reflect.TypeToken;
import com.tty.api.*;
import com.tty.api.dto.AliasItem;
import com.tty.api.enumType.FilePathEnum;
import com.tty.api.service.*;
import com.tty.api.state.StateService;
import com.tty.api.utils.CommandRegister;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.function.PlayerTabManager;
import com.tty.listener.*;
import com.tty.listener.bungee.GetServerListListener;
import com.tty.listener.home.EditHomeListener;
import com.tty.listener.home.HomeListListener;
import com.tty.listener.player.*;
import com.tty.listener.teleport.RecordLastLocationListener;
import com.tty.listener.unsupported.SandDupeListener;
import com.tty.listener.warp.EditWarpListener;
import com.tty.listener.warp.WarpListListener;
import com.tty.tool.*;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class Ari extends BaseJavaPlugin {

    private static final int PLUGIN_ID = 29755;

    public static Ari instance;
    public static SQLInstance SQL_INSTANCE;
    public static RepositoryManager REPOSITORY_MANAGER;
    public static PermissionService PERMISSION_SERVICE;
    public static EconomyService ECONOMY_SERVICE;
    public static ConfigDataService DATA_SERVICE;
    public static NBTDataService NBT_DATA_SERVICE;
    public static FireworkService FIREWORK_SERVICE;
    public static TeleportingService TELEPORTING_SERVICE;
    public static InteractService INTERACT_SERVICE;
    public static StateMachineManager STATE_MACHINE_MANAGER;
    public static Placeholder PLACEHOLDER;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.printLogo();
        this.loadOtherPlugins();

        SQL_INSTANCE = new SQLInstance();
        REPOSITORY_MANAGER = new RepositoryManager(this);
        STATE_MACHINE_MANAGER = new StateMachineManager();

        this.registerListener();
        this.registerBungeeListener();
        CommandRegister.register(this, "com.tty.commands", FormatUtils.yamlConvertToObj(this.getConfigInstance().getObject(FilePath.COMMAND_ALIAS.name()).saveToString(), new TypeToken<Map<String, AliasItem>>() {}.getType()));

        PLACEHOLDER = new Placeholder();
        this.initMetrics();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (STATE_MACHINE_MANAGER != null) {
            STATE_MACHINE_MANAGER.forEach(StateService::abort);
        }

        if (REPOSITORY_MANAGER != null) {
            REPOSITORY_MANAGER.clearAllCache();
            REPOSITORY_MANAGER.stop();
        }

        if (SQL_INSTANCE != null) {
            SQL_INSTANCE.close();
        }

    }

    @Override
    protected FilePathEnum[] fileList() {
        return FilePath.values();
    }

    private void loadOtherPlugins() {
        PublicFunctionUtils.loadPlugin("arilib", PermissionService.class, i -> PERMISSION_SERVICE = i);
        PublicFunctionUtils.loadPlugin("arilib", EconomyService.class, i -> ECONOMY_SERVICE = i);
        PublicFunctionUtils.loadPlugin("arilib", ConfigDataService.class, i -> DATA_SERVICE = i);
        PublicFunctionUtils.loadPlugin("arilib", NBTDataService.class, i -> NBT_DATA_SERVICE = i);
        PublicFunctionUtils.loadPlugin("arilib", FireworkService.class, i -> FIREWORK_SERVICE = i);
        PublicFunctionUtils.loadPlugin("arilib", TeleportingService.class, i -> TELEPORTING_SERVICE = i);
        PublicFunctionUtils.loadPlugin("arilib", InteractService.class, i -> INTERACT_SERVICE = i);
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
        pluginManager.registerEvents(new SandDupeListener(), this);
    }

    private void registerBungeeListener() {
        Messenger messenger = Bukkit.getMessenger();
        messenger.registerOutgoingPluginChannel(this, "BungeeCord");
        messenger.registerIncomingPluginChannel(this, "BungeeCord", new GetServerListListener());
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
        Ari.instance.getLog().info("");
        Ari.instance.getLog().info("    ___    ____   ___");
        Ari.instance.getLog().info("   /   |  / __ \\ |   |");
        Ari.instance.getLog().info("  / /| | / /_/ / |   |  {}", pluginInfo);
        Ari.instance.getLog().info(" / ___ |/ _, _/  |   |  Running on {} {}", bukkitName, bukkitVersion);
        Ari.instance.getLog().info("/_/  |_/_/ |_|   |___|");
        Ari.instance.getLog().info("");

    }

    private void initMetrics() {
        try {
            new Metrics(this, PLUGIN_ID);
            this.getLog().debug("loaded metrics.");
        } catch (Exception e) {
            this.getLog().warn(e, "unable to load metrics");
        }
    }

}
