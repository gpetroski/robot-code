package com.test.robotcontroller.event.serial;

/**
 * Created by greg on 11/25/14.
 */
public class HalfDuplexState {
    public static final int LOCK_DENIED = 251;
    public static final int RELINQUISH_LOCK = 252;
    public static final int LOCK_GRANTED = 253;
    public static final int MESSAGE_CONFIRMED = 254;
    public static final int REQUEST_LOCK = 255;

    boolean running = false;
    SerialPhase phase = SerialPhase.IDLE;
    SerialMode mode = SerialMode.IDLE;

    public boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public SerialPhase getPhase() {
        return phase;
    }

    public synchronized void setPhase(SerialPhase phase) {
        this.phase = phase;
    }

    public SerialMode getMode() {
        return mode;
    }

    public synchronized void setMode(SerialMode mode) {
        this.mode = mode;
    }

    public synchronized void resetModeAndPhase() {
        this.phase = SerialPhase.IDLE;
        this.mode = SerialMode.IDLE;
    }
}
