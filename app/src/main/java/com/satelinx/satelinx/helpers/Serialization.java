package com.satelinx.satelinx.helpers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.satelinx.satelinx.models.User;
import com.satelinx.satelinx.models.typeAdapters.UserTypeAdapter;

/**
 * Created by jlh on 1/17/15.
 */
public class Serialization {

    public static Gson getGsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapter(User.class, new UserTypeAdapter())
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public static String getPrettyPrintedString(Object o) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(o);
    }
}
