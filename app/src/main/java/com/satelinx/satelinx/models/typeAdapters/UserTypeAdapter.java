package com.satelinx.satelinx.models.typeAdapters;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.satelinx.satelinx.models.User;

import java.lang.reflect.Type;

/**
 * Created by jlh on 1/2/15.
 */
public class UserTypeAdapter implements JsonDeserializer<User> {

    @Override
    public User deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException
    {
        JsonElement user = je;
        JsonObject body = je.getAsJsonObject();
        if (body != null && body.has("user")) {
            user = body.get("user");
        }
        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
        // to this deserializer
        return new Gson().fromJson(user, User.class);

    }
}
