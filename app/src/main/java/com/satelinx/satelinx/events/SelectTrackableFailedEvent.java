package com.satelinx.satelinx.events;

import com.satelinx.satelinx.models.Trackable;

/**
 * Created by jlh on 2/25/15.
 */
public class SelectTrackableFailedEvent {

    public static final int STATUS_UNAUTHORIZED = 1;
    public static final int STATUS_EMPTY_LIST = 2;

    public int status;
    public Trackable trackable;

    public SelectTrackableFailedEvent(Trackable t, int status) {
        this.status = status;
        this.trackable = t;
    }
}
