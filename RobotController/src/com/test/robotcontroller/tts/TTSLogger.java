package com.test.robotcontroller.tts;

import java.util.Locale;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TTSLogger implements Runnable {
	private static final int QUEUE_SIZE = 5;
	private static TTSLogger currentLogger;
	private TextToSpeech tts;
	CircularFifoQueue<String> messages = new CircularFifoQueue<String>(QUEUE_SIZE);
	private boolean running = false;
	private Thread thisThread;
	
	public TTSLogger(TextToSpeech tts) {
		this.tts = tts;
		thisThread = new Thread(this);
		thisThread.start();
	}
	
	public void logMessage(String message) {
        Log.i("TTS", message);
        messages.add(message);
	}
	
	public void shutdown() {
		running = false;
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

	@Override
	public void run() {
		this.running = true;
		while(running) {
			if(!tts.isSpeaking() && messages.size() > 0) {
				tts.speak(messages.poll(), TextToSpeech.QUEUE_ADD, null);
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}
}
