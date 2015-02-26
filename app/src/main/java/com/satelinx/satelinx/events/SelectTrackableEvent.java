package com.satelinx.satelinx.events;

import com.satelinx.satelinx.models.Trackable;

/**
 * Created by jlh on 2/25/15.
 */
public class SelectTrackableEvent {

    public Trackable trackable;
    public String date;

    public SelectTrackableEvent(Trackable trackable, String date) {
        this.trackable = trackable;
        this.date = date;
    }
}
