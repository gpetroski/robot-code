package com.test.robotcontroller.event.outgoing;

import com.test.robotcontroller.event.Event;

/**
 * Created by greg on 11/25/14.
 */
public class WriteEvent extends Event {
    Byte message;

    public Byte getMessage() {
        return message;
    }

    public void setMessage(Byte message) {
        this.message = message;
    }
}
