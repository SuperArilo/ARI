package com.tty;

import com.google.gson.reflect.TypeToken;
import com.tty.api.BaseJavaPlugin;
import com.tty.api.ServerPlatform;
import com.tty.api.dto.AliasItem;
import com.tty.api.dto.TempRegisterService;
import com.tty.api.enumType.FilePathEnum;
import com.tty.api.service.*;
import com.tty.api.state.StateService;
import com.tty.api.utils.CommandRegister;
import com.tty.api.utils.FormatUtils;
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
import com.tty.tool.Placeholder;
import com.tty.tool.RepositoryManager;
import com.tty.tool.SQLInstance;
import com.tty.tool.StateMachineManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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

        SQL_INSTANCE = new SQLInstance();
        REPOSITORY_MANAGER = new RepositoryManager(this);
        STATE_MACHINE_MANAGER = new StateMachineManager();

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
    protected List<TempRegisterService<?>> loadOtherPlugin() {
        return List.of(
                TempRegisterService.of("arilib", PermissionService.class, i -> PERMISSION_SERVICE = i),
                TempRegisterService.of("arilib", EconomyService.class, i -> ECONOMY_SERVICE = i),
                TempRegisterService.of("arilib", ConfigDataService.class, i -> DATA_SERVICE = i),
                TempRegisterService.of("arilib", NBTDataService.class, i -> NBT_DATA_SERVICE = i),
                TempRegisterService.of("arilib", FireworkService.class, i -> FIREWORK_SERVICE = i),
                TempRegisterService.of("arilib", TeleportingService.class, i -> TELEPORTING_SERVICE = i),
                TempRegisterService.of("arilib", InteractService.class, i -> INTERACT_SERVICE = i)
        );
    }

    @Override
    protected @NotNull List<Listener> registerEvents() {
        return List.of(
                new DamageTrackerListener(),
                new GuiCleanupListener(),
                new GuiCleanupListener(),
                new HomeListListener(GuiType.HOME_LIST),
                new EditHomeListener(GuiType.HOME_EDIT),
                new RecordLastLocationListener(),
                new PlayerListener(),
                new WarpListListener(GuiType.WARP_LIST),
                new EditWarpListener(GuiType.WARP_EDIT),
                new OnPlayerJoinAndLeaveListener(),
                new PlayerSkipNight(),
                new OnPluginReloadListener(),
                new PlayerTabManager(),
                new CustomChatFormantListener(),
                new PlayerActionListener(),
                new KeepInventoryAndExperience(),
                new CustomPlayerDeathListener(),
                new BreakAndExplodeListener(),
                new AutoSeedListener(),
                new MobBossBarListener(),
                new DisableMobSpawnListener(),
                new CustomTotemCostListener(),
                new SandDupeListener()
        );
    }

    @Override
    protected @NotNull FilePathEnum @NotNull [] fileList() {
        return FilePath.values();
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
