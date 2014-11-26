package com.test.robotcontroller.event;

import java.util.Date;

/**
 * Created by greg on 11/25/14.
 */
public class Event {
    Date timestamp = new Date();

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
