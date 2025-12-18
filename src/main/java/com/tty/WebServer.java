package com.tty;

import com.tty.web.ControllerRegistrar;
import com.tty.web.controllers.ServerStatusController;
import io.javalin.Javalin;
import org.bukkit.plugin.java.JavaPlugin;

public class WebServer {

    private Javalin app;


    public void start(JavaPlugin plugin) {
        app = Javalin.create(config -> config.bundledPlugins.enableCors(cors -> {
            cors.addRule(rule -> {
                rule.allowHost("http://localhost:8080");
                rule.allowCredentials = true;
            });
        })).start("0.0.0.0", 11451);
        ControllerRegistrar.register(app, new ServerStatusController());
    }


    public void stop() {
        if (app != null) app.stop();
    }

}
