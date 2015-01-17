package com.satelinx.satelinx.models;

import com.google.gson.JsonObject;
import com.satelinx.satelinx.helpers.Serialization;

/**
 * Created by jlh on 1/17/15.
 */
public class Trackable {
    String identifier;
    String type;
    String sub_type;
    String driver_name;
    int max_speed;
    int refresh_rate;
    String path_color;

    Coordinate last_coordinate;


    public JsonObject toJson() {
        return Serialization.getGsonInstance().toJsonTree(this).getAsJsonObject();
    }
}
