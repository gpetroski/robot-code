package com.test.robotcontroller.event.handler;

import android.util.Log;

import com.test.robotcontroller.event.incoming.ProximityEvent;
import com.test.robotcontroller.proximity.RobotProximityQueue;

import javax.inject.Inject;

/**
 * Created by greg on 11/25/14.
 */
public class ProximityEventHandler {
    private static final String LOG_TAG = ProximityEventHandler.class.getCanonicalName();
    @Inject
    RobotProximityQueue proximityQueue;

    public void onEvent(ProximityEvent proximity) {
        Log.d(LOG_TAG, "Received proximity: " + String.valueOf(proximity.getProximity()));
        proximityQueue.queueProximity(proximity);
    }
}
