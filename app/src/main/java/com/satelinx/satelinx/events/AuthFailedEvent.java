package com.satelinx.satelinx.events;

/**
 * Created by jlh on 1/2/15.
 */
public class AuthFailedEvent extends AuthEvent {

    public AuthFailedEvent(String username) {
        super(username);
    }
}
