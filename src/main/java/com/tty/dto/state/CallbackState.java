package com.tty.dto.state;

import com.tty.lib.dto.state.StateCondition;
import com.tty.dto.TeleportState;
import com.tty.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class CallbackState extends TeleportState {

    private final StateCondition customCondition;
    private final Runnable successCallback;
    @Getter
    private final TeleportType type;

    public CallbackState(Entity owner, TeleportType type, int max_count, StateCondition customCondition, Runnable successCallback) {
        super(owner, type, max_count);
        this.customCondition = customCondition;
        this.successCallback = successCallback;
        this.type = type;
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
