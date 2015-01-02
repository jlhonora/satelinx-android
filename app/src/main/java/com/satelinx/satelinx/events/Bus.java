package com.satelinx.satelinx.events;

import de.greenrobot.event.EventBus;

/**
 * Created by jlh on 1/2/15.
 */
public final class Bus {

    private static final EventBus bus = new EventBus();

    public static final EventBus getInstance() {
        return bus;
    }

}
