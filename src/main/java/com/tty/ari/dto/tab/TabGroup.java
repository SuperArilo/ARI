package com.tty.ari.dto.tab;

import org.bukkit.entity.Player;

import java.util.List;

public record TabGroup(TabGroupLine line, List<Player> players) { }

