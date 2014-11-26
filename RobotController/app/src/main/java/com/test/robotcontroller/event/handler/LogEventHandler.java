package com.test.robotcontroller.event.handler;

import android.util.Log;

import com.test.robotcontroller.event.incoming.LogEvent;
import com.test.robotcontroller.tts.TTSLogger;

import javax.inject.Inject;

/**
 * Created by greg on 11/25/14.
 */
public class LogEventHandler {
    private static final String LOG_TAG = LogEventHandler.class.getCanonicalName();
    @Inject
    TTSLogger ttsLogger;

    public void onEvent(LogEvent event) {
        Log.i(LOG_TAG, event.getMessage());
        ttsLogger.logMessage(event.getMessage());
    }
}
