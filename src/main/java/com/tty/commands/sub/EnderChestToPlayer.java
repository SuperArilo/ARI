package com.tty.commands.sub;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.command.RequiredArgumentCommand;
import com.tty.entity.ServerPlayer;
import com.tty.gui.OfflineNBTEnderCheat;
import com.tty.api.Log;
import com.tty.api.annotations.ArgumentCommand;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.PublicFunctionUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@CommandMeta(displayName = "name or uuid (string)", permission = "ari.command.enderchest.player", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class EnderChestToPlayer extends RequiredArgumentCommand<String> {

    public static final List<UUID> OFFLINE_ON_EDIT_ENDER_CHEST_LIST = new CopyOnWriteArrayList<>();

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) return;
        Player player = (Player) sender;
        UUID uuid = PublicFunctionUtils.parseUUID(args[1]);
        if (uuid == null) {
            sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return;
        }
        Ari.REPOSITORY_MANAGER.get(ServerPlayer.class)
            .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid.toString()))
            .thenCompose(serverPlayer -> CompletableFuture.completedFuture(serverPlayer != null))
            .thenAccept(status -> {
                if (!status) {
                    sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                    return;
                }
                if (OFFLINE_ON_EDIT_ENDER_CHEST_LIST.contains(uuid)) {
                    sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.task-occupied")));
                    return;
                }
                Player b = Bukkit.getServer().getPlayer(uuid);
                if (b == null) {
                    Log.debug("player {} is offline to open ender chest.", uuid.toString());
                    OFFLINE_ON_EDIT_ENDER_CHEST_LIST.add(uuid);
                    Ari.SCHEDULER.runAsync(Ari.instance, i -> {
                        NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(uuid.toString());
                        if (data == null) {
                            sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                            Log.error("uuid is not exist.", uuid.toString());
                            return;
                        }
                        ReadWriteNBTCompoundList enderItems = data.getCompoundList("EnderItems");
                        OfflineNBTEnderCheat cheat = new OfflineNBTEnderCheat(player, data, uuid);
                        Ari.SCHEDULER.runAtEntity(Ari.instance, player, t -> {
                            cheat.open();
                            for (ReadWriteNBT enderItem : enderItems) {
                                int slot = enderItem.getByte("Slot") & 0xFF;
                                ItemStack itemStack = NBT.itemStackFromNBT(enderItem);
                                cheat.setItem(slot, itemStack);
                            }
                        }, () -> {
                            Log.error("read player {} nbt error.");
                            OFFLINE_ON_EDIT_ENDER_CHEST_LIST.remove(uuid);
                        });
                    });
                } else {
                    player.openInventory(b.getEnderChest());
                }
            });
    }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Set<String> strings = onlinePlayers.stream().map(Player::getName).collect(Collectors.toSet());
        if (onlinePlayers.isEmpty() || args.length != 3) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[2], strings));
    }
}
