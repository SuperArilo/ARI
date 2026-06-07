package com.tty.ari.tool;

import com.tty.api.state.State;
import com.tty.api.state.StateService;
import com.tty.ari.states.CoolDownStateService;
import com.tty.ari.states.GuiEditStateService;
import com.tty.ari.states.GuiManagerStateService;
import com.tty.ari.states.PlayerSaveStateService;
import com.tty.ari.states.action.PlayerRideActionStateService;
import com.tty.ari.states.action.PlayerSitActionStateService;
import com.tty.ari.states.teleport.PreTeleportStateService;
import com.tty.ari.states.teleport.RandomTpStateService;
import com.tty.ari.states.teleport.TeleportStateService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StateMachineManager {

    private final Map<Class<? extends StateService<?>>, StateService<? extends State>> stateMachines = new HashMap<>();

    public StateMachineManager() {
        this.registerStateMachine(new PreTeleportStateService(20L, 1L, false));
        this.registerStateMachine(new TeleportStateService(20L, 1L, false));
        this.registerStateMachine(new CoolDownStateService(20L, 1L, true));
        this.registerStateMachine(new PlayerSitActionStateService(20L, 1L, false));
        this.registerStateMachine(new PlayerRideActionStateService(20L, 1L, false));
        this.registerStateMachine(new RandomTpStateService(20L, 1L, true));
        this.registerStateMachine(new GuiEditStateService(20L,1L, false));
        this.registerStateMachine(new PlayerSaveStateService(20L, 20L, true));
        this.registerStateMachine(new GuiManagerStateService(20L, 1L, false));
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