package com.test.robotcontroller.event.serial;

import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by greg on 11/25/14.
 *
 lock denied 253

 -> request Lock 255
 <- lock granted 251
 -> write message
 <- message received 254
 -> give up lock 252
 */
public class HalfDuplexWritingThread implements Runnable {
    private static final String LOG_TAG = HalfDuplexWritingThread.class.getCanonicalName();
    private static final int ERROR_THRESHOLD = 5;
    HalfDuplexState state;
    OutputStream outputStream;
    CircularFifoQueue<Byte> queue = new CircularFifoQueue<Byte>(10);

    public HalfDuplexWritingThread(OutputStream outputStream, HalfDuplexState state) {
        this.outputStream = outputStream;
        this.state = state;
    }

    public void write(Byte message) {
        queue.add(message);
    }

    @Override
    public void run() {
        doWrites();
    }


    private void doWrites() {
        int errorCount = 0;
        while(state.isRunning()) {
            try {
                switch (state.getMode()) {
                    case IDLE:
                        doIdleWrite();
                        break;
                    case SENDING:
                        doSendingWrite();
                        break;
                    case RECEIVING:
                        doReceivingWrite();
                        break;
                }
            } catch(Exception ex) {
                Log.e(LOG_TAG, ex.getMessage(), ex);
                errorCount++;
                state.resetModeAndPhase();
                if(errorCount > ERROR_THRESHOLD) {
                    Log.e(LOG_TAG, "Too many write errors occurred. Shutting down half duplex serial.");
                    state.setRunning(false);
                }
            }
        }
    }

    private void doSendingWrite() throws IOException {
        switch (state.getPhase()) {
            case LOCK_GRANTED:
                Log.d(LOG_TAG, "In LOCK_GRANTED. Writing next value. Setting MESSAGE_WRITTEN");
                state.setPhase(SerialPhase.MESSAGE_WRITTEN);
                doWrite(queue.poll().intValue());
            case MESSAGE_CONFIRMED:
                Log.d(LOG_TAG, "In MESSAGE_CONFIRMED. Setting LOCK_RELINQUISHED");
                state.setPhase(SerialPhase.LOCK_RELINQUISHED);
                doWrite(HalfDuplexState.RELINQUISH_LOCK);
                state.resetModeAndPhase();
        }
    }

    private void doReceivingWrite() throws IOException {
        switch (state.getPhase()) {
            case LOCK_REQUESTED:
                Log.d(LOG_TAG, "In LOCK_REQUESTED. Setting LOCK_GRANTED");
                state.setPhase(SerialPhase.LOCK_GRANTED);
                doWrite(HalfDuplexState.LOCK_GRANTED);
            case MESSAGE_WRITTEN:
                Log.d(LOG_TAG, "In MESSAGE_WRITTEN. Setting MESSAGE_CONFIRMED");
                state.setPhase(SerialPhase.MESSAGE_CONFIRMED);
                doWrite(HalfDuplexState.MESSAGE_CONFIRMED);
        }

    }

    private void doWrite(Integer message) throws IOException {
        outputStream.write(message);
        outputStream.flush();
    }

    private void doIdleWrite() throws IOException {
        if (queue.isEmpty()) return;
        Log.d(LOG_TAG, "In IDLE mode. Setting mode to SENDING. Setting LOCK_REQUESTED");
        state.setMode(SerialMode.SENDING);
        state.setPhase(SerialPhase.LOCK_REQUESTED);
        doWrite(HalfDuplexState.REQUEST_LOCK);
    }
}
