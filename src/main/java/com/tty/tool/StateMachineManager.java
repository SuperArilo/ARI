package com.tty.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.tty.lib.dto.state.State;
import com.tty.lib.services.StateService;
import com.tty.states.*;
import com.tty.states.action.PlayerRideActionStateService;
import com.tty.states.action.PlayerSitActionStateService;
import com.tty.states.teleport.PreTeleportStateService;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.states.teleport.TeleportStateService;
import org.bukkit.plugin.java.JavaPlugin;

public class StateMachineManager {

    private final JavaPlugin plugin;
    private final Map<Class<? extends StateService<?>>, StateService<? extends State>> stateMachines = new HashMap<>();

    public StateMachineManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    public <T extends StateService<? extends State>> void registerStateMachine(T machine) {
        this.stateMachines.put((Class<? extends StateService<?>>) machine.getClass(), machine);
    }

    public void initDefaultStateMachines() {
        this.registerStateMachine(new PreTeleportStateService(20L, 1L, false, this.plugin));
        this.registerStateMachine(new TeleportStateService(20L, 1L, false, this.plugin));
        this.registerStateMachine(new CoolDownStateService(20L, 1L, true, this.plugin));
        this.registerStateMachine(new RandomTpStateService(20L, 1L, true, this.plugin));
        this.registerStateMachine(new PlayerSitActionStateService(20L, 1L, false, this.plugin));
        this.registerStateMachine(new PlayerRideActionStateService(20L, 1L, false, this.plugin));
        this.registerStateMachine(new GuiEditStateService(20L,1L, false, this.plugin));
        this.registerStateMachine(new PlayerSaveStateService(20L, 20L, true, this.plugin));
    }

    @SuppressWarnings("unchecked")
    public <T extends StateService<? extends State>> T get(Class<T> clazz) {
        return (T) this.stateMachines.get(clazz);
    }

    public void forEach(Consumer<StateService<? extends State>> action) {
        this.stateMachines.values().forEach(action);
    }
}