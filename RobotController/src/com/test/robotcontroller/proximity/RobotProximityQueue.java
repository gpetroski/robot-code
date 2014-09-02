package com.test.robotcontroller.proximity;

import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;

import com.google.gson.Gson;
import com.test.robotcontroller.bluetooth.messages.RobotLogMessage;
import com.test.robotcontroller.bluetooth.messages.RobotProximityMessage;

public class RobotProximityQueue {
	private static final int QUEUE_SIZE = 10;
	private static final String LOG_TAG = RobotProximityQueue.class.getCanonicalName();
	ConcurrentLinkedQueue<RobotProximityMessage> proximityReadings = new ConcurrentLinkedQueue<RobotProximityMessage>();
	
	public void queueMessage(String message) {
		try {
			Gson gson = new Gson();	
			if(message.contains("PROXIMITY")) {
				proximityReadings.add(gson.fromJson(message, RobotProximityMessage.class));
				if(proximityReadings.size() > QUEUE_SIZE) {
					proximityReadings.poll();
				}
			} else if(message.contains("LOG")) {
                Log.i(LOG_TAG, gson.fromJson(message, RobotLogMessage.class).getMessage());
			}
		} catch(Exception ex) {
			Log.e(LOG_TAG, "Unable to parse message", ex);
		}
	}
	
	public int getAvgReading() {
		int sum = 0;
		if(proximityReadings.size() >= QUEUE_SIZE) {
			for(int i = 0; i < QUEUE_SIZE; i++) {
				sum += proximityReadings.poll().getValue();
			}
			return sum / QUEUE_SIZE;
		} else {
			return -1;
		}
	}

	public void clearProximities() {
		proximityReadings.clear();
	}
}
