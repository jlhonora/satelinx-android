package com.satelinx.satelinx.events;

/**
 * Created by jlh on 1/2/15.
 */
public class AuthRequestEvent extends AuthEvent {

    String password;

    public AuthRequestEvent(String username, String password) {
        super(username);
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }
}
