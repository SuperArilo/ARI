package com.tty.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.tty.api.state.State;
import com.tty.api.state.StateService;
import com.tty.states.*;
import com.tty.states.action.PlayerRideActionStateService;
import com.tty.states.action.PlayerSitActionStateService;
import com.tty.states.teleport.PreTeleportStateService;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.states.teleport.TeleportStateService;

public class StateMachineManager {

    private final Map<Class<? extends StateService<?>>, StateService<? extends State>> stateMachines = new HashMap<>();

    public StateMachineManager() {
        this.registerStateMachine(new PreTeleportStateService(20L, 1L, false));
        this.registerStateMachine(new TeleportStateService(20L, 1L, false));
        this.registerStateMachine(new CoolDownStateService(20L, 1L, true));
        this.registerStateMachine(new RandomTpStateService(20L, 1L, true));
        this.registerStateMachine(new PlayerSitActionStateService(20L, 1L, false));
        this.registerStateMachine(new PlayerRideActionStateService(20L, 1L, false));
        this.registerStateMachine(new GuiEditStateService(20L,1L, false));
        this.registerStateMachine(new PlayerSaveStateService(20L, 20L, true));
    }

    @SuppressWarnings("unchecked")
    public <T extends StateService<? extends State>> void registerStateMachine(T machine) {
        this.stateMachines.put((Class<? extends StateService<?>>) machine.getClass(), machine);
    }

    @SuppressWarnings("unchecked")
    public <T extends StateService<? extends State>> T get(Class<T> clazz) {
        return (T) this.stateMachines.get(clazz);
    }

    public void forEach(Consumer<StateService<? extends State>> action) {
        this.stateMachines.values().forEach(action);
    }

}