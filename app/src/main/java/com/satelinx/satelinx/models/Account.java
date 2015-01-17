package com.satelinx.satelinx.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.satelinx.satelinx.helpers.Serialization;

import java.util.List;

/**
 * Created by jlh on 1/17/15.
 */
public class Account {
    @Expose
    long id;

    @Expose
    String name;

    @Expose
    List<Trackable> trackables;

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public JsonObject toJson() {
        return Serialization.getGsonInstance().toJsonTree(this).getAsJsonObject();
    }

    public String toString() {
        return this.getName();
    }

    public List<Trackable> getTrackables() {
        return this.trackables;
    }
}
