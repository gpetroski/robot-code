package com.test.robotcontroller.event.serial;

/**
 * Created by greg on 11/25/14.
 */
public enum SerialPhase {
    IDLE,
    LOCK_REQUESTED,
    LOCK_GRANTED,
    MESSAGE_WRITTEN,
    MESSAGE_CONFIRMED,
    LOCK_RELINQUISHED;

}
