package com.satelinx.satelinx.events;

import com.google.gson.JsonObject;

/**
 * Created by jlh on 1/2/15.
 */
public class AuthSuccessEvent extends AuthEvent {

    public JsonObject userJson;

    public AuthSuccessEvent(String username) {
        super(username);
    }

    public AuthSuccessEvent(String username, JsonObject userJson) {
        super(username);
        this.userJson = userJson;
    }
}
