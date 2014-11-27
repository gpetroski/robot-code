package com.test.robotcontroller.event.incoming;

import com.test.robotcontroller.event.Event;

/**
 * Created by greg on 11/25/14.
 */
public class ReadEvent extends Event {
    private Integer message;

    public Integer getMessage() {
        return message;
    }

    public void setMessage(Integer message) {
        this.message = message;
    }
}
