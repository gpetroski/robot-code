package com.test.robotcontroller.event.incoming;

import com.test.robotcontroller.event.Event;

/**
 * Created by greg on 11/25/14.
 */
public class LogEvent extends Event {
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String message;
}
