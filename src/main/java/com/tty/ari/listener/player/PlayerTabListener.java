package com.tty.ari.listener.player;

import com.tty.api.ComponentTool;
import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.api.scheduler.RunTask;
import com.tty.ari.Ari;
import com.tty.ari.configuration.TabListConfig;
import com.tty.ari.dto.tab.TabGroup;
import com.tty.ari.dto.tab.TabGroupLine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerTabListener implements Listener {

    private boolean isWarning = false;

    private RunTask runTask;

    // 每次更新 tab 的周期，单位 tick
    private int updateInterval;
    private boolean enable;

    private final List<String> headers = new ArrayList<>();
    private final List<String> footers = new ArrayList<>();

    private final Map<String, TabGroupLine> groupLines = new HashMap<>();
    private final List<String> groupOrder = new ArrayList<>();

    private static final String DEFAULT_GROUP = "_default_";
    private static final JoinConfiguration NEW_LINE = JoinConfiguration.separator(Component.newline());

    private void start() {
        this.stop();
        if (!this.enable) {
            this.reorderTabList();
            return;
        }

        this.runTask = Ari.instance.getScheduler().runAtFixedRate(
                i -> this.updateTab(new ArrayList<>(Bukkit.getServer().getOnlinePlayers())),
                1L,
                this.updateInterval
        );
    }

    private void stop() {
        if (this.runTask != null) {
            this.runTask.cancel();
            this.runTask = null;
        }
    }

    private void updateTab(Collection<? extends Player> players) {
        if (players.isEmpty()) return;

        Map<String, List<Player>> grouped = this.groupPlayers(players);
        AtomicInteger order = new AtomicInteger(Bukkit.getMaxPlayers());

        for (String group : this.groupOrder) {
            List<Player> list = grouped.get(group);
            if (list == null || list.isEmpty()) continue;

            list.sort(Comparator.comparing(Player::getName));
            TabGroupLine line = this.groupLines.getOrDefault(group, new TabGroupLine("", ""));
            TabGroup tabGroup = new TabGroup(line, list);

            for (Player player : tabGroup.players()) {
                this.applyTab(player, tabGroup, order.getAndDecrement());
            }
        }
    }

    private void applyTab(Player player, TabGroup group, int order) {
        player.sendPlayerListHeaderAndFooter(
                buildComponent(this.headers, player),
                buildComponent(this.footers, player)
        );
        player.playerListName(ComponentTool.text(group.line().prefix() + player.getName() + group.line().suffix()));
        try {
            player.setPlayerListOrder(order);
        } catch (Exception e) {
            if (!this.isWarning) {
                this.isWarning = true;
                Ari.instance.getLog().warn("setPlayerListOrder() requires Minecraft 1.21.3+, current version is lower. Feature disabled.");
            }
        }
    }

    private Map<String, List<Player>> groupPlayers(Collection<? extends Player> players) {
        Map<String, List<Player>> result = new LinkedHashMap<>();

        for (String group : this.groupOrder) {
            result.put(group, new ArrayList<>());
        }

        for (Player player : players) {
            String group = this.resolveGroup(player);
            result.get(group).add(player);
        }

        return result;
    }

    private String resolveGroup(Player player) {
        for (String group : this.groupOrder) {
            if (Ari.PERMISSION_SERVICE.getPlayerIsInGroup(player, group)) {
                return group;
            }
        }
        return DEFAULT_GROUP;
    }

    private Component buildComponent(List<String> lines, Player player) {
        if (lines.isEmpty()) return Component.empty();
        return Component.join(NEW_LINE, lines.stream().map(line -> ComponentTool.text(line, player)).toList());
    }

    private void reorderTabList() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.forEach(player -> {
            player.playerListName(player.name());
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        });
        onlinePlayers.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        for (int i = 0; i < onlinePlayers.size(); i++) {
            onlinePlayers.get(i).setPlayerListOrder(i);
        }
    }

    private void reloadConfig() {

        TabListConfig tabListConfig = Ari.instance.getConfigurationManager().get(TabListConfig.class);

        this.enable = tabListConfig.isEnable();
        this.updateInterval = tabListConfig.updateInterval();

        this.headers.clear();
        this.footers.clear();
        this.groupLines.clear();
        this.groupOrder.clear();

        this.headers.addAll(tabListConfig.getLayoutHeader());
        this.footers.addAll(tabListConfig.getLayoutFooter());

        this.groupLines.putAll(tabListConfig.getGroups());
        this.groupOrder.addAll(tabListConfig.getSlot());

        if (!this.groupOrder.contains(DEFAULT_GROUP)) {
            this.groupOrder.add(DEFAULT_GROUP);
        }
    }

    @EventHandler
    public void onReload(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        this.reloadConfig();
        this.start();
    }

}
