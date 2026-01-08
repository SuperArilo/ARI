package com.tty.commands.sub.enchant;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.commands.args.enchant.EnchantLevelArgs;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.PublicFunctionUtils;
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

public class EnchantShowList extends BaseRequiredArgumentLiteralCommand<String> {

    public EnchantShowList() {
        super(2, StringArgumentType.string(), true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnchantLevelArgs());
    }

    @Override
    public String name() {
        return "enchant";
    }

    @Override
    public String permission() {
        return "ari.command.enchant";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
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
