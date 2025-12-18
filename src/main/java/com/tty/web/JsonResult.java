package com.tty.web;


import io.javalin.http.HttpStatus;

import java.util.HashMap;


public class JsonResult extends HashMap<String, Object> {

    private JsonResult() {
        this.put(CommonConstant.CODE, HttpStatus.OK);
        this.put(CommonConstant.MESSAGE, CommonConstant.SUCCESS);
        this.put(CommonConstant.DATA, null);
    }

    private JsonResult(HttpStatus code) {
        this();
        this.put(CommonConstant.CODE, code);
    }

    private JsonResult(HttpStatus code, String message) {
        this(code);
        this.put(CommonConstant.MESSAGE, message);
    }

    private JsonResult(HttpStatus code, String message, Object data) {
        this(code, message);
        this.put(CommonConstant.DATA, data);
    }

    public static JsonResult OK() {
        return OK(CommonConstant.SUCCESS);
    }

    public static JsonResult OK(String message) {
        return OK(message, null);
    }

    public static JsonResult OK(String message, Object data) {
        return new JsonResult(HttpStatus.OK, message, data);
    }

    public static JsonResult OK(Object data) {
        return OK(CommonConstant.SUCCESS, data);
    }

    public static JsonResult ERROR(HttpStatus code, String message) {
        return ERROR(code, message, null);
    }

    public static JsonResult ERROR(HttpStatus code,String message, Object data) {
        return new JsonResult(code, message, data);
    }

    @Override
    public JsonResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }

}
