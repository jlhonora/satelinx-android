package com.satelinx.satelinx.events;

import com.satelinx.satelinx.models.Coordinate;
import com.satelinx.satelinx.models.Trackable;

import java.util.List;

/**
 * Created by jlh on 2/25/15.
 */
public class SelectTrackableReadyEvent {

    public Trackable trackable;
    public List<Coordinate> coordinates;

    public SelectTrackableReadyEvent(Trackable t, List<Coordinate> c) {
        trackable = t;
        this.coordinates = c;
    }

}
