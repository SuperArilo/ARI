package com.tty.tool;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.tty.Ari;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.dto.AliasItem;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class CommandRegister {

    public static void register(JavaPlugin plugin, String packagePath, Map<String, AliasItem> aliasItemMap) {
        Ari.LOG.debug("----------register commands ----------");
        long start = System.currentTimeMillis();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            aliasItemMap.forEach((k, v) -> {
                if(!v.isEnable()) return;
                Class<?> executorClass;
                try {
                    executorClass = Class.forName(packagePath + "." + k, true, plugin.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    Ari.LOG.error("Error while constructing instruction. {} class not found!", k);
                    return;
                }
                Object executorInstance;
                try {
                    executorInstance = executorClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    Ari.LOG.error(e, "Error while constructing executor for instruction: {}", k);
                    return;
                }
                if (executorInstance instanceof SuperHandsomeCommand cmd) {
                    commands.register((LiteralCommandNode<CommandSourceStack>) cmd.toBrigadier(), v.getUsage());
                    Ari.LOG.debug((v.isEnable() ? "":"un" ) + "register command: {}", k);
                }
            });
            Ari.LOG.debug("register commands time: {}ms", (System.currentTimeMillis() - start));
        });
    }

}
