package com.test.robotcontroller.event.incoming;

import com.test.robotcontroller.event.Event;

/**
 * Created by greg on 11/25/14.
 */
public class ReadEvent extends Event {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
