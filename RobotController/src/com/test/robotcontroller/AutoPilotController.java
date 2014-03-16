package com.test.robotcontroller;

import android.util.Log;

import com.test.robotcontroller.bluetooth.BluetoothService;
import com.test.robotcontroller.bluetooth.messages.RobotMessageQueue;
import com.test.robotcontroller.bluetooth.messages.outgoing.RobotMoveMessage;
import com.test.robotcontroller.tts.TTSLogger;

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
			try {
				adjustHeading(getAvgReading());
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	}

	public boolean isRunning() {
		return running;
	}
	
	public void stop() {
		this.running = false;
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	move(new RobotMoveMessage(RobotMoveMessage.Direction.STOP, RobotMoveMessage.Speed.FULL));
	}
	
	private void move(RobotMoveMessage message) {
		moveMessage = message;
		bluetooth.sendMessage(moveMessage.getMessage());
	}
	
	private int getAvgReading() {
		int sum = 0;
		if(proximities.getProximityCount() > MAX_SIZE) {
			TTSLogger.log("Queue is backing up!");
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
	
	private void adjustHeading(int proximity) throws InterruptedException {
		if(proximity < TOO_CLOSE && proximity > 0) {
			TTSLogger.log("Obstacle detected!");
			TTSLogger.log("Reversing");
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.REVERSE, RobotMoveMessage.Speed.MEDIUM));
			Thread.sleep(1000);
			TTSLogger.log("Turning right");
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.RIGHT, RobotMoveMessage.Speed.FULL));
			Thread.sleep(1000);
			TTSLogger.log("Stopping");
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.STOP, RobotMoveMessage.Speed.FULL));
		} else if (moveMessage == null || moveMessage.getDirection() != RobotMoveMessage.Direction.FORWARD) {
			TTSLogger.log("Moving forward");
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.FORWARD, RobotMoveMessage.Speed.FULL));
		}
	}
}
