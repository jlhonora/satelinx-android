package com.satelinx.satelinx.events;

/**
 * Created by jlh on 1/2/15.
 */
public class AuthSuccessEvent extends AuthEvent {

    public AuthSuccessEvent(String username) {
        super(username);
    }
}
