package com.tty.entity.sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseEntity {

    public Object deepClone() {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(gson.toJson(this), this.getClass());
    }

}
