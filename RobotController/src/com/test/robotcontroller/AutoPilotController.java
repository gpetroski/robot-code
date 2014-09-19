package com.test.robotcontroller;

import android.util.Log;

import com.test.robotcontroller.bluetooth.BluetoothService;
import com.test.robotcontroller.bluetooth.messages.outgoing.RobotMessageType;
import com.test.robotcontroller.bluetooth.messages.outgoing.RobotMoveMessage;
import com.test.robotcontroller.proximity.RobotProximityQueue;
import com.test.robotcontroller.tts.TTSLogger;

public class AutoPilotController implements Runnable {
	private static final String LOG_TAG = AutoPilotController.class.getCanonicalName();
	private static final int TOO_CLOSE = 20;
	private RobotProximityQueue proximities;
	private BluetoothService bluetooth;
	private boolean running = false;
	private RobotMoveMessage moveMessage;
	private ProximityRequestor proximityRequestor;
	
	public AutoPilotController(RobotProximityQueue proximities, BluetoothService bluetooth) {
		this.proximities = proximities;
		this.bluetooth = bluetooth;
		proximityRequestor = new ProximityRequestor(bluetooth);
	}
	
	@Override
	public void run() {
		new Thread(proximityRequestor).start(); 
		running = true;
		while(running) {
			try {
				adjustHeading(proximities.getAvgReading());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
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
		this.proximityRequestor.stop();
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
	
	private void adjustHeading(int proximity) throws InterruptedException {
		if(proximity < TOO_CLOSE && proximity > 0) {
			TTSLogger.log("Obstacle detected!");
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.REVERSE, RobotMoveMessage.Speed.MEDIUM));
			Thread.sleep(1000);
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.RIGHT, RobotMoveMessage.Speed.FULL));
			Thread.sleep(1000);
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.STOP, RobotMoveMessage.Speed.FULL));
		} else if (moveMessage == null || moveMessage.getDirection() != RobotMoveMessage.Direction.FORWARD) {
        	move(new RobotMoveMessage(RobotMoveMessage.Direction.FORWARD, RobotMoveMessage.Speed.FULL));
		}
	}
	
	private static class ProximityRequestor implements Runnable {
		private BluetoothService bluetooth;
		private boolean running = false;
		
		public ProximityRequestor(BluetoothService bluetooth) {
			this.bluetooth = bluetooth;
		}
		
		@Override
		public void run() {
			running = true;
			while(running) {
				bluetooth.sendSynchronousMessage((byte)RobotMessageType.GET_PING_MESSAGE_TYPE.getIntegerValue());
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}
		
		public void stop() {
			running = false;
		}
		
	}
}
