package com.test.robotcontroller.event.incoming;

import com.test.robotcontroller.event.Event;

/**
 * Created by greg on 11/25/14.
 */
public class ProximityEvent extends Event {
    Integer proximity;

    public Integer getProximity() {
        return proximity;
    }

    public void setProximity(Integer proximity) {
        this.proximity = proximity;
    }
}
