package com.tty.web.controllers;

import com.tty.annotations.Get;
import com.tty.annotations.RequestMapping;
import com.tty.annotations.RequireToken;
import com.tty.annotations.WebController;
import com.tty.entity.web.ServerStatus;
import com.tty.web.JsonResult;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

@WebController @RequestMapping("/api")
public class ServerStatusController {
    @SneakyThrows
    @RequireToken(value = false)
    @Get("/server/status")
    public Object serverStatus() {

        ServerStatus status = new ServerStatus();

        double mspt = Bukkit.getServer().getAverageTickTime();
        double tps  = Math.min(20.0, 1000.0 / mspt);

        status.setMspt(Math.round(mspt * 100.0) / 100.0);
        status.setTps(Math.round(tps * 100.0) / 100.0);
        status.setTimestamp(System.currentTimeMillis());
        status.setPlayers(Bukkit.getOnlinePlayers().size());
        Runtime runtime = Runtime.getRuntime();
        status.setUsedMemory((double)(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);

        return JsonResult.OK(status);
    }
}