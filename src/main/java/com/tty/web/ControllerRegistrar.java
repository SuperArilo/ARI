package com.tty.web;

import com.tty.annotations.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ControllerRegistrar {


    public static void register(Javalin app, Object controller) {


        Class<?> clazz = controller.getClass();
        if (!clazz.isAnnotationPresent(WebController.class)) return;


        String basePath = "";
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            basePath = clazz.getAnnotation(RequestMapping.class).value();
        }


        for (Method method : clazz.getDeclaredMethods()) {


            if (method.isAnnotationPresent(Get.class)) {
                String path = basePath + method.getAnnotation(Get.class).value();
                app.get(path, ctx -> handle(controller, method, ctx));
            }


            if (method.isAnnotationPresent(Post.class)) {
                String path = basePath + method.getAnnotation(Post.class).value();
                app.post(path, ctx -> handle(controller, method, ctx));
            }
        }
    }


    private static void handle(Object controller, Method method, Context ctx) {
        boolean requireToken = method.isAnnotationPresent(RequireToken.class)
                && method.getAnnotation(RequireToken.class).value();
        if (requireToken) {
            ctx.status(401).result("Unauthorized");
            return;
        }
        Object[] params = Arrays.stream(method.getParameters())
                .map(p -> p.isAnnotationPresent(RequestBody.class) ? ctx.bodyAsClass(Map.class) : null)
                .toArray();
        ctx.future(() -> {
            try {
                Object ret = method.invoke(controller, params);

                if (ret instanceof CompletableFuture<?> future) {
                    return future.thenApply(result -> {
                        if (result != null) ctx.json(result);
                        return result;
                    });
                } else {
                    CompletableFuture<Object> completed = new CompletableFuture<>();
                    if (ret != null) ctx.json(ret);
                    completed.complete(ret);
                    return completed;
                }
            } catch (Exception e) {
                CompletableFuture<Object> ex = new CompletableFuture<>();
                ex.completeExceptionally(e);
                return ex;
            }
        });
    }
}
