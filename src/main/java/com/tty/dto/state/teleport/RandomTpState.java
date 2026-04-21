package com.tty.dto.state.teleport;

import com.tty.api.state.AsyncState;
import com.tty.enumType.TeleportType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class RandomTpState extends AsyncState {

    @Getter
    private final World world;
    @Getter
    private final TeleportType type = TeleportType.RTP;

    @Getter
    @Setter
    private Location trueLocation;

    public RandomTpState(Entity owner, int max_count, @NotNull World world) {
        super(owner, max_count);
        this.world = world;
    }
}
