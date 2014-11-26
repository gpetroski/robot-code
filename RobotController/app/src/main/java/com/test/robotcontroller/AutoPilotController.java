package com.test.robotcontroller;

import android.util.Log;

import com.test.robotcontroller.bluetooth.BluetoothService;
import com.test.robotcontroller.event.outgoing.MoveEvent;
import com.test.robotcontroller.event.outgoing.RobotMessageType;
import com.test.robotcontroller.proximity.RobotProximityQueue;
import com.test.robotcontroller.tts.TTSLogger;

public class AutoPilotController implements Runnable {
	private static final String LOG_TAG = AutoPilotController.class.getCanonicalName();
	private static final int TOO_CLOSE = 20;
	private RobotProximityQueue proximities;
	private BluetoothService bluetooth;
	private boolean running = false;
    private MoveEvent moveEvent;
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
    	move(new MoveEvent(MoveEvent.Direction.STOP, MoveEvent.Speed.FULL));
	}
	
	private void move(MoveEvent message) {
        moveEvent = message;
		bluetooth.sendMessage(moveEvent.getMessage());
	}
	
	private void adjustHeading(int proximity) throws InterruptedException {
		if(proximity < TOO_CLOSE && proximity > 0) {
			TTSLogger.log("Obstacle detected!");
        	move(new MoveEvent(MoveEvent.Direction.REVERSE, MoveEvent.Speed.MEDIUM));
			Thread.sleep(1000);
        	move(new MoveEvent(MoveEvent.Direction.RIGHT, MoveEvent.Speed.FULL));
			Thread.sleep(1000);
        	move(new MoveEvent(MoveEvent.Direction.STOP, MoveEvent.Speed.FULL));
		} else if (moveEvent == null || moveEvent.getDirection() != MoveEvent.Direction.FORWARD) {
        	move(new MoveEvent(MoveEvent.Direction.FORWARD, MoveEvent.Speed.FULL));
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
				bluetooth.sendMessage((byte)RobotMessageType.GET_PING_MESSAGE_TYPE.getIntegerValue());
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
