package com.test.robotcontroller;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.test.robotcontroller.bluetooth.BluetoothService;

public class AutoPilotController implements Runnable {
	private static final int QUEUE_SIZE = 10;
	private static final int TOO_CLOSE = 15;
	private ConcurrentLinkedQueue<Integer> proxReadings = new ConcurrentLinkedQueue<Integer>();
	private BluetoothService bluetoothService;
	
	@Override
	public void run() {
		String message = bluetoothService.getNextMessage();
		if(message != null) {
			queueProximity(message);
		}
		int avgReading = getAvgReading();
		if(avgReading == -1) {
			
		}
	}

	public void queueProximity(String message) {
		int prox;
		try {
			prox = Integer.parseInt(message);
			if(proxReadings.size() >= QUEUE_SIZE) {
				proxReadings.poll();
			}
			proxReadings.add(prox);
		} catch(Exception ex) {
			
		}
	}
	
	public int getAvgReading() {
		int sum = 0;
		Iterator<Integer> proxIt = proxReadings.iterator();
		while(proxIt.hasNext()) {
			sum += proxIt.next();
		}
		if(sum <= 0) {
			return -1;
		} else {
			return sum / proxReadings.size();
		}
	}
}
