package com.test.robotcontroller.tts;

import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TTSLogger {
	private static TTSLogger currentLogger;
	private TextToSpeech tts;
	
	public TTSLogger(TextToSpeech tts) {
		this.tts = tts;
	}
	
	public void logMessage(String message) {
        Log.i("TTS", message);
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	public void shutdown() {
        tts.stop();
        tts.shutdown();
	}
	
	public int setLanguage(Locale locale) {
		return tts.setLanguage(locale);
	}
	
	public int setPitch(float pitch) {
		return tts.setPitch(pitch);
	}
	
	public static void log(String message) {
		currentLogger.logMessage(message);
	}
	
	public static void setCurrentLogger(TTSLogger logger) {
		currentLogger = logger;
	}
}
