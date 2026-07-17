package com.tty.ari;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.tty.api.AbstractJavaPlugin;
import com.tty.api.ConfigurationManager;
import com.tty.api.scheduler.Scheduler;
import com.tty.api.StatusManager;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.configuration.BaseConfiguration;
import com.tty.api.dto.AliasItem;
import com.tty.api.dto.CommandAlias;
import com.tty.api.dto.TempRegisterService;
import com.tty.api.service.*;
import com.tty.api.state.StateService;
import com.tty.ari.configuration.*;
import com.tty.ari.configuration.home.HomeConfig;
import com.tty.ari.configuration.home.HomeEditConfig;
import com.tty.ari.configuration.home.HomeGuiConfig;
import com.tty.ari.configuration.lang.DeathMessageLang;
import com.tty.ari.configuration.lang.LangConfig;
import com.tty.ari.configuration.warp.WarpConfig;
import com.tty.ari.configuration.warp.WarpEditGuiConfig;
import com.tty.ari.configuration.warp.WarpGuiConfig;
import com.tty.ari.dto.BungeeCache;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.listener.*;
import com.tty.ari.listener.bungee.GetServerListListener;
import com.tty.ari.listener.home.EditHomeListener;
import com.tty.ari.listener.home.HomeListListener;
import com.tty.ari.listener.player.*;
import com.tty.ari.listener.player.check.InventoryCheckListener;
import com.tty.ari.listener.player.check.PlayerInventoryUpdateListener;
import com.tty.ari.listener.teleport.RecordLastLocationListener;
import com.tty.ari.listener.unsupported.SandDupeListener;
import com.tty.ari.listener.warp.EditWarpListener;
import com.tty.ari.listener.warp.WarpListListener;
import com.tty.ari.states.*;
import com.tty.ari.states.action.PlayerRideActionStateService;
import com.tty.ari.states.action.PlayerSitActionStateService;
import com.tty.ari.states.gui.GuiEditFunctionStateService;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.states.teleport.PreTeleportStateService;
import com.tty.ari.states.teleport.RandomTpStateService;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.Placeholder;
import com.tty.ari.tool.RepositoryManager;
import com.tty.ari.tool.SQLInstance;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class Ari extends AbstractJavaPlugin {

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
    public static Placeholder PLACEHOLDER;
    public static BungeeCache BUNGEECACHE;

    public static Metrics METRICS;

    @Override
    protected void loading() {
        instance = this;
    }

    @Override
    protected void enabling() {
        this.printLogo();

        SQL_INSTANCE = new SQLInstance();
        REPOSITORY_MANAGER = new RepositoryManager(this);

        this.registerBungeeListener();

        this.registerCommand();

        PLACEHOLDER = new Placeholder(this);
        BUNGEECACHE = new BungeeCache(this);
        this.initMetrics();
    }

    @Override
    protected void disabling() {
        StatusManager statusManager = this.getStatusManager();
        if (statusManager != null) {
            statusManager.abort();
        }

        if (REPOSITORY_MANAGER != null) {
            REPOSITORY_MANAGER.clearAllCache();
            REPOSITORY_MANAGER.stop();
        }

        if (SQL_INSTANCE != null) {
            SQL_INSTANCE.close();
        }
        if (BUNGEECACHE != null) {
            BUNGEECACHE.shutdown();
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
                new HomeListListener(GuiType.HOME_LIST),
                new EditHomeListener(GuiType.HOME_EDIT),
                new RecordLastLocationListener(),
                new PlayerListener(),
                new WarpListListener(GuiType.WARP_LIST),
                new EditWarpListener(GuiType.WARP_EDIT),
                new InventoryCheckListener(this, GuiType.PLAYER_INVENTORY_EDIT),
                new PlayerInventoryUpdateListener(),
                new OnPlayerJoinAndLeaveListener(),
                new PlayerSkipNight(),
                new OnPluginReloadListener(),
                new PlayerTabListener(),
                new CustomChatFormantListener(),
                new PlayerActionListener(),
                new KeepInventoryAndExperience(),
                new CustomPlayerDeathListener(),
                new BreakAndExplodeListener(),
                new AutoSeedListener(),
                new MobBossBarListener(),
                new DisableMobSpawnListener(),
                new CustomTotemCostListener(),
                new SandDupeListener(),
                new PlayerCommandCoolDownListener(),
                new PlayerAFKStatusListener()
        );
    }

    @Override
    protected @Nullable List<BaseConfiguration> configurations() {
        return List.of(
                new LangConfig(),
                new DeathMessageLang(),
                new HomeConfig(),
                new HomeGuiConfig(),
                new HomeEditConfig(),
                new WarpConfig(),
                new WarpGuiConfig(),
                new WarpEditGuiConfig(),
                new AttackBarConfig(),
                new ChatConfig(),
                new CheckInventoryLayoutConfig(),
                new CommandAliasConfig(),
                new FunctionConfig(),
                new GameActionConfig(),
                new TabListConfig()
        );
    }

    @Override
    protected @Nullable List<StateService<?>> services() {
        ArrayList<StateService<?>> list = new ArrayList<>();
        list.add(new PreTeleportStateService(20L, 1L, false));
        list.add(new TeleportStateService(20L, 1L, false));
        list.add(new CoolDownStateService(20L, 1L, true));
        list.add(new PlayerSitActionStateService(20L, 1L, false));
        list.add(new PlayerRideActionStateService(20L, 1L, false));
        list.add(new RandomTpStateService(20L, 1L, true));
        list.add(new GuiEditFunctionStateService(20L,1L, false));
        list.add(new PlayerAFKService(20L, 1L, true));
        list.add(new PlayerSaveDataStateService(20L, 20L, true));
        list.add(new GuiManagerStateService(10L, 1L, false));
        list.add(new PlayerCommandPreprocessService(20L, 1L, true));
        list.add(new PlayerChatService(20L, 1L, true));
        list.add(new MaintenanceBossBarService(20L, 1L, true));
        list.add(new AttackBossBarService(5L, 1L, true));
        list.add(new PlayerVanishService(5L, 1L, false));
        return list;
    }

    private void registerCommand() {
        ConfigurationManager manager = this.getConfigurationManager();
        CommandAlias aliasConfig = manager.yamlConvertToObj(manager.get(CommandAliasConfig.class).getConfiguration().saveToString(), CommandAlias.class);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();

            if (aliasConfig == null) {
                String pluginName = this.getName().toLowerCase();
                try {
                    Object instance = Class.forName("com.tty.ari.commands" + "." + pluginName, true, this.getClass().getClassLoader()).getDeclaredConstructor().newInstance();
                    if (instance instanceof SuperHandsomeCommand cmd) {
                        commands.register((LiteralCommandNode<CommandSourceStack>) cmd.toBrigadier());
                    }
                } catch (Exception e) {
                    this.getLog().error(e, "Failed to register root command: {}", pluginName);
                }
                return;
            }

            Map<String, AliasItem> aliasItemMap = aliasConfig.getAliases();
            if (aliasItemMap == null) return;
            aliasItemMap.forEach((k, v) -> {
                if (!v.isEnable()) return;
                Class<?> executorClass;
                try {
                    executorClass = Class.forName("com.tty.ari.commands" + "." + k, true, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    this.getLog().error("Error while constructing instruction. {} class not found!", k);
                    return;
                }
                Object executorInstance;
                try {
                    executorInstance = executorClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    this.getLog().error(e, "Error while constructing executor for instruction: {}", k);
                    return;
                }
                if (executorInstance instanceof SuperHandsomeCommand cmd) {
                    try {
                        LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) cmd.toBrigadier();
                        commands.register(node, v.getUsage());
                        this.getLog().debug("register command: {}", k);
                    } catch (Exception e) {
                        this.getLog().error(e, "Failed to register command: {}", k);
                    }
                }
            });
        });
    }

    private void registerBungeeListener() {
        Messenger messenger = Bukkit.getMessenger();
        messenger.registerOutgoingPluginChannel(this, "BungeeCord");
        messenger.registerIncomingPluginChannel(this, "BungeeCord", new GetServerListListener());
    }

    private void printLogo() {
        String pluginInfo;
        if (Scheduler.isFolia()) {
            PluginMeta pluginMeta = Ari.instance.getPluginMeta();
            pluginInfo = pluginMeta.getName() + " " + pluginMeta.getVersion();
        } else {
            PluginDescriptionFile description = Ari.instance.getDescription();
            pluginInfo = description.getName() + " " + description.getVersion();
        }
        Logger logger = Bukkit.getLogger();
        logger.log(Level.INFO, "");
        logger.log(Level.INFO,"    ___    ____   ___");
        logger.log(Level.INFO,"   /   |  / __ \\ |   |");
        logger.log(Level.INFO,"  / /| | / /_/ / |   |  {0}", pluginInfo);
        logger.log(Level.INFO," / ___ |/ _, _/  |   |  Running on {0} {1}.", new Object[]{Bukkit.getName(), Bukkit.getServer().getVersion()});
        logger.log(Level.INFO,"/_/  |_/_/ |_|   |___|");
        logger.log(Level.INFO,"");

    }

    private void initMetrics() {
        try {
            METRICS = new Metrics(this, PLUGIN_ID);
            this.getLog().debug("loaded metrics.");
        } catch (Exception e) {
            this.getLog().warn(e, "unable to load metrics");
        }
    }

}
