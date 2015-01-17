package com.satelinx.satelinx.models;

import com.google.gson.JsonObject;
import com.satelinx.satelinx.helpers.Serialization;

/**
 * Created by jlh on 1/17/15.
 */
public class Trackable {
    public String identifier;
    public String type;
    public String sub_type;
    public String driver_name;
    int max_speed;
    int refresh_rate;
    public String path_color;

    Coordinate last_coordinate;


    public JsonObject toJson() {
        return Serialization.getGsonInstance().toJsonTree(this).getAsJsonObject();
    }

    public Coordinate getLastCoordinate() {
        return this.last_coordinate;
    }
}
