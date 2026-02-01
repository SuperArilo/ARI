package com.tty.function;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.event.CustomPluginReloadEvent;
import com.tty.dto.tab.TabGroup;
import com.tty.dto.tab.TabGroupLine;
import com.tty.enumType.FilePath;
import com.tty.api.task.CancellableTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerTabManager implements Listener {

    private CancellableTask task;

    // 每次更新 tab 的周期，单位 tick
    private int updateInterval;
    private boolean enable;

    private final List<String> headers = new ArrayList<>();
    private final List<String> footers = new ArrayList<>();

    private final Map<String, TabGroupLine> groupLines = new HashMap<>();
    private final List<String> groupOrder = new ArrayList<>();

    private static final String DEFAULT_GROUP = "_default_";
    private static final JoinConfiguration NEW_LINE = JoinConfiguration.separator(Component.newline());

    public PlayerTabManager() {
        this.reloadConfig();
        this.start();
    }

    private void start() {
        this.stop();
        if (!this.enable) return;

        this.task = Ari.SCHEDULER.runAtFixedRate(
                Ari.instance,
                i -> this.updateTab(Bukkit.getOnlinePlayers()),
                1L,
                this.updateInterval
        );
    }

    private void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    private void updateTab(Collection<? extends Player> players) {
        if (players.isEmpty()) return;

        Map<String, List<Player>> grouped = groupPlayers(players);
        AtomicInteger order = new AtomicInteger(Bukkit.getMaxPlayers());

        for (String group : this.groupOrder) {
            List<Player> list = grouped.get(group);
            if (list == null || list.isEmpty()) continue;

            list.sort(Comparator.comparing(Player::getName));
            TabGroupLine line = this.groupLines.getOrDefault(group, new TabGroupLine("", ""));
            TabGroup tabGroup = new TabGroup(line, list);

            for (Player player : tabGroup.players()) {
                applyTab(player, tabGroup, order.getAndDecrement());
            }
        }
    }

    private void applyTab(Player player, TabGroup group, int order) {
        player.sendPlayerListHeaderAndFooter(
                buildComponent(this.headers, player),
                buildComponent(this.footers, player)
        );
        player.playerListName(ComponentUtils.text(group.line().prefix() + player.getName() + group.line().suffix()));
        player.setPlayerListOrder(order);
    }

    private Map<String, List<Player>> groupPlayers(Collection<? extends Player> players) {
        Map<String, List<Player>> result = new LinkedHashMap<>();

        for (String group : this.groupOrder) {
            result.put(group, new ArrayList<>());
        }

        for (Player player : players) {
            String group = resolveGroup(player);
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
        return Component.join(NEW_LINE, lines.stream().map(line -> ComponentUtils.text(line, player)).toList());
    }

    private void reloadConfig() {
        this.enable = Ari.C_INSTANCE.getValue("tab.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
        this.updateInterval = Ari.C_INSTANCE.getValue("tab.update-interval", FilePath.FUNCTION_CONFIG, Integer.class, 20);

        this.headers.clear();
        this.footers.clear();
        this.groupLines.clear();
        this.groupOrder.clear();

        TypeToken<List<String>> listType = new TypeToken<>() {};

        this.headers.addAll(Ari.C_INSTANCE.getValue("tab.layout.header", FilePath.FUNCTION_CONFIG, listType.getType(), List.of()));
        this.footers.addAll(Ari.C_INSTANCE.getValue("tab.layout.footer", FilePath.FUNCTION_CONFIG, listType.getType(), List.of()));

        this.groupLines.putAll(Ari.C_INSTANCE.getValue("tab.groups", FilePath.FUNCTION_CONFIG, new TypeToken<Map<String, TabGroupLine>>() {}.getType(), Map.of()));
        this.groupOrder.addAll(Ari.C_INSTANCE.getValue("tab.slot", FilePath.FUNCTION_CONFIG, listType.getType(), List.of()));

        if (!this.groupOrder.contains(DEFAULT_GROUP)) {
            this.groupOrder.add(DEFAULT_GROUP);
        }
    }

    @EventHandler
    public void onReload(CustomPluginReloadEvent event) {
        this.reloadConfig();
        this.start();
    }
}
