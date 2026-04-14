package com.tty.dto.state;

import com.tty.api.state.StateCondition;
import com.tty.dto.TeleportState;
import com.tty.enumType.TeleportType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CallbackState extends TeleportState {

    private final StateCondition customCondition;
    private final Runnable successCallback;

    public CallbackState(Entity owner, TeleportType type, Location location, int max_count, StateCondition customCondition, Runnable successCallback) {
        super(owner, type, location, max_count);
        this.customCondition = customCondition;
        this.successCallback = successCallback;
    }

    public boolean checkCondition() {
        return customCondition == null || customCondition.test();
    }

    public void executeCallback() {
        if (successCallback != null) {
            successCallback.run();
        }
    }
}
