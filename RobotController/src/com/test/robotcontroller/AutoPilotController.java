package com.test.robotcontroller;

import android.util.Log;

import com.test.robotcontroller.bluetooth.BluetoothService;
import com.test.robotcontroller.bluetooth.messages.RobotMessageQueue;
import com.test.robotcontroller.bluetooth.messages.RobotMoveMessage;

public class AutoPilotController implements Runnable {
	private static final String LOG_TAG = AutoPilotController.class.getCanonicalName();
	private static final int QUEUE_SIZE = 10;
	private static final int MAX_SIZE = 100;
	private static final int TOO_CLOSE = 20;
	private RobotMessageQueue proximities;
	private BluetoothService bluetooth;
	private boolean running = false;
	private RobotMoveMessage moveMessage;
	
	
	public AutoPilotController(RobotMessageQueue proximities, BluetoothService bluetooth) {
		this.proximities = proximities;
		this.bluetooth = bluetooth;
	}
	
	@Override
	public void run() {
		running = true;
		while(running) {
			adjustHeading(getAvgReading());
		}
	}

	public boolean isRunning() {
		return running;
	}
	
	public void stop() {
		this.running = false;
		move("S", 1);
	}
	
	private void move(String direction, float power) {
		moveMessage = new RobotMoveMessage(direction, power);
		bluetooth.sendMessage(moveMessage.toJson());
	}
	
	private int getAvgReading() {
		int sum = 0;
		if(proximities.getProximityCount() > MAX_SIZE) {
			Log.e(LOG_TAG, "Queue is backing up!!!");
		}
		if(proximities.getProximityCount() >= QUEUE_SIZE) {
			for(int i = 0; i < QUEUE_SIZE; i++) {
				sum += proximities.getNextProximity().getValue();
			}
			return sum / QUEUE_SIZE;
		} else {
			return -1;
		}
	}
	
	private void adjustHeading(int proximity) {
		if(proximity < TOO_CLOSE && proximity > 0) {
            move("B", .5f);
            move("R", 1);
            move("S", 1);
		} else if (moveMessage == null || moveMessage.getDirection() != "F") {
			move("F", 1);
		}
	}
}
