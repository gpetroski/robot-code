package com.test.robotcontroller.event.handler;

import android.util.Log;

import com.test.robotcontroller.event.incoming.LogEvent;
import com.test.robotcontroller.event.serial.HalfDuplexSerialController;

import javax.inject.Inject;

/**
 * Created by greg on 11/25/14.
 */
public class MoveEventHandler {
    private static final String LOG_TAG = MoveEventHandler.class.getCanonicalName();
    @Inject
    HalfDuplexSerialController serialController;

    public void onEvent(LogEvent event) {
        Log.i(LOG_TAG, event.getMessage());
    }


}
