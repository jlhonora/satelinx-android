package com.satelinx.satelinx.models;

import android.content.Context;

import com.google.gson.JsonObject;
import com.satelinx.satelinx.helpers.Serialization;

/**
 * Created by jlh on 1/17/15.
 */
public class Trackable {
    public long id;
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

    public boolean hasValidLastCoordinate() {
        // Exact 0.0 coordinates are usually an error
        if (last_coordinate == null          ||
                last_coordinate.latitude  == 0.0 ||
                last_coordinate.longitude == 0.0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.identifier;
    }

    public String getSnippet(Context ctx) {
        String snippet = "";
        if (this.driver_name != null) {
            snippet += this.driver_name + "\r\n";
        }
        if (!this.hasValidLastCoordinate()) {
            return snippet;
        }
        Coordinate lastCoordinate = this.last_coordinate;

        snippet += lastCoordinate.getSnippet(ctx);
        return snippet;
    }
}
