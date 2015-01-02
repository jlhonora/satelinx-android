package com.satelinx.satelinx.events;

/**
 * Created by jlh on 1/2/15.
 */
public abstract class AuthEvent {

    protected String username;

    /**
     * Describes an auth event.
     *
     * @param username The username for this auth event
     */
    public AuthEvent(String username) {
        this.username = username;
    }

    /**
     * Check if this events corresponds a certain username.
     *
     * @param username
     * @return true if this event matches the username
     */
    public boolean matchesUsername(String username) {
        if (username == null || this.username == null) {
            return false;
        }
        return username.contentEquals(this.username);
    }

    public String getUsername() {
        return username;
    }
}
