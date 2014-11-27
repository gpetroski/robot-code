package com.test.robotcontroller.event.serial;

import android.util.Log;

import com.test.robotcontroller.event.incoming.ReadEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import de.greenrobot.event.EventBus;

/**
 * Created by greg on 11/25/14.
 *
 <- request lock 255
 -> lock granted
 <- read message
 -> message received 254
 <- give up lock 252

 */
public class HalfDuplexReadingThread implements Runnable {
    private static final String LOG_TAG = HalfDuplexReadingThread.class.getCanonicalName();
    private static final int ERROR_THRESHOLD = 5;
    HalfDuplexState state;
    InputStream inputStream;
    EventBus eventBus;

    public HalfDuplexReadingThread(EventBus eventBus, InputStream inputStream, HalfDuplexState state) {
        this.inputStream = inputStream;
        this.state = state;
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        doReads();
    }

    public void doReads() {
        int errorCount = 0;
        int input;

        while(state.isRunning()) {
            try {
                if((input = inputStream.read()) <= 0) {
                    continue;
                }
                switch (state.getMode()) {
                    case IDLE:
                        doIdleRead(input);
                        break;
                    case SENDING:
                        doSendingRead(input);
                        break;
                    case RECEIVING:
                        doReceivingRead(input);
                        break;
                }
            } catch(Exception ex) {
                Log.e(LOG_TAG, ex.getMessage(), ex);
                errorCount++;
                if(errorCount > ERROR_THRESHOLD) {
                    Log.e(LOG_TAG, "Too many read errors occurred. Shutting down half duplex serial.");
                    state.setRunning(false);
                }
            }
        }
    }

    private void doIdleRead(Integer input) throws IOException {
        switch (input) {
            case HalfDuplexState.REQUEST_LOCK:
                state.setMode(SerialMode.RECEIVING);
                state.setPhase(SerialPhase.LOCK_REQUESTED);
                Log.d(LOG_TAG, "Lock requested");
                break;
            default:
                Log.d(LOG_TAG, "Ignoring input in IDLE " + input);
        }
    }

    private void doSendingRead(Integer input) {
        switch (input) {
            case HalfDuplexState.LOCK_GRANTED:
                state.setPhase(SerialPhase.LOCK_GRANTED);
                Log.d(LOG_TAG, "Lock granted");
                break;
            case HalfDuplexState.MESSAGE_CONFIRMED:
                state.setPhase(SerialPhase.MESSAGE_CONFIRMED);
                Log.d(LOG_TAG, "Message confirmed");
            default:
                Log.d(LOG_TAG, "Ignoring input in SENDING " + input);
        }

    }

    private void doReceivingRead(Integer input) {
        if(input == HalfDuplexState.RELINQUISH_LOCK) {
            Log.d(LOG_TAG, "Relinquishing lock");
            state.resetModeAndPhase();
        } else {
            Log.d(LOG_TAG, "Input received: " + input);
            ReadEvent readEvent = new ReadEvent();
            readEvent.setMessage(input);
            eventBus.post(readEvent);
        }
    }

    private Integer getIntegerValue(String input) {
        try {
            if(input.length() > 0 && input.matches("[0-9]+")) {
                return Integer.valueOf(input);
            }
        } catch (Exception ex) { /* Ignore */  }
        return null;
    }
}
