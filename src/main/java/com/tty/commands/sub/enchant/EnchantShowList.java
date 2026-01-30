package com.tty.commands.sub.enchant;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.command.RequiredArgumentCommand;
import com.tty.commands.args.enchant.EnchantLevelArgs;
import com.tty.api.annotations.ArgumentCommand;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.PublicFunctionUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandMeta(displayName = "enchant", permission = "ari.command.enchant", tokenLength = 3)
@ArgumentCommand(isSuggests = true)
public class EnchantShowList extends RequiredArgumentCommand<String> {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnchantLevelArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) { }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Set<@NotNull String> collect = registry.stream().map(i -> i.key().value()).collect(Collectors.toSet());
        if (args.length == 1) {
            return CompletableFuture.completedFuture(collect);
        } else if (args.length == 2) {
            return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[1], collect));
        }

        return CompletableFuture.completedFuture(Set.of());
    }

}
