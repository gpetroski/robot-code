package com.test.robotcontroller.event.serial;

import android.util.Log;

import com.test.robotcontroller.event.outgoing.WriteEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.greenrobot.event.EventBus;

/**
 * Created by greg on 11/25/14.
 */
public class HalfDuplexSerialController {
    private static final String LOG_TAG = HalfDuplexSerialController.class.getCanonicalName();
    HalfDuplexReadingThread readingThread;
    HalfDuplexWritingThread writingThread;
    HalfDuplexState state;

    OutputStream outputStream;
    InputStream inputStream;
    EventBus eventBus;

    public HalfDuplexSerialController(EventBus eventBus, InputStream inputStream, OutputStream outputStream) {
        this.eventBus = eventBus;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void start() {
        Log.i(LOG_TAG, "Starting HalfDuplexSerialController reading and writing threads.");
        state = new HalfDuplexState();
        state.setRunning(true);
        readingThread = new HalfDuplexReadingThread(eventBus, inputStream, state);
        writingThread = new HalfDuplexWritingThread(outputStream, state);
        new Thread(readingThread).start();
        new Thread(writingThread).start();
    }

    public void stop() {
        Log.i(LOG_TAG, "Stopping HalfDuplexSerialController reading and writing threads.");
        state.setRunning(false);
        try {
            inputStream.close();
            outputStream.close();
        } catch(IOException ex) {
            // Ignore
        }
    }

    public void onEvent(WriteEvent writeEvent) {
        writingThread.write(writeEvent.getMessage());
    }
}
