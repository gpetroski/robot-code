package com.test.robotcontroller.bluetooth.messages;

import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;

import com.google.gson.Gson;

public class RobotMessageQueue {
	private static final String LOG_TAG = RobotMessageQueue.class.getCanonicalName();
	ConcurrentLinkedQueue<RobotProximityMessage> proximityReadings = new ConcurrentLinkedQueue<RobotProximityMessage>();
	
	public void queueMessage(String message) {
		try {
			Gson gson = new Gson();	
			if(message.contains("PROXIMITY")) {
				proximityReadings.add(gson.fromJson(message, RobotProximityMessage.class));
			} else if(message.contains("LOG")) {
                Log.i(LOG_TAG, gson.fromJson(message, RobotLogMessage.class).getMessage());
			}
		} catch(Exception ex) {
			Log.e(LOG_TAG, "Unable to parse message", ex);
		}
	}
	
	public RobotProximityMessage getNextProximity() {
		return proximityReadings.poll();
	}
	
	public int getProximityCount() {
		return proximityReadings.size();
	}

	public void clearProximities() {
		proximityReadings.clear();
	}
}
